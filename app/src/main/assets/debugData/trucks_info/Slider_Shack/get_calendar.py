import httplib2
import pdb
import argparse
from datetime import timedelta, datetime, tzinfo, time
import urllib2
import re

import MySQLdb
import MySQLdb.cursors 

from apiclient.discovery import build
from oauth2client.file import Storage
from oauth2client.client import OAuth2WebServerFlow
from oauth2client.tools import run

def finish(args,total=0, fail=0, skip=0, success = 0):
    print "------------DONE: name: %s url: %s  total: %s success: %s skip:%s fail: %s-------------------" % (args.name,args.url,total, success, skip, fail)



parser = argparse.ArgumentParser(description='Process some integers.')
#parser.add_argument('--cID', type=str, dest='calendarId', action='store', help='calendarId', required=True)
#parser.add_argument('--from', type=str, dest='startDay', action='store',help='start date', required=True)
#parser.add_argument('--to', type=str, dest='endDay', action='store',help='end date', required=True)
parser.add_argument('--name', type=str, dest='name', action='store',help='truck name', required=True)
parser.add_argument('--url', type=str, dest='url', action='store',help='website url', required=True)

args = parser.parse_args()
total = 0
fail = 0
skip = 0
success = 0
start_date = None
end_date = None
date = None
start_time = None
end_time = None
address = None
meal = None
sql_args = None
skip_events = ("close", "private", "grid", "otg", "night market")

print "------------START: name %s  url %s----------------" % (args.name, args.url)

url = args.url

reponse = urllib2.urlopen(url)

html = reponse.read()

google_calendar_pattern = re.compile(r'https://www.google.com/calendar.*?src=(?P<cID>[0-9a-zA-Z].*?((%40)|@).*?\.com)')

try: calendarId = google_calendar_pattern.search(html).groupdict()['cID'].replace("%40", "@")
except Exception, e: 
    print "~~~~~~ERROR: fail to get calendar ID~~~~~"
    print e
    finish(args)


print "~~~~~~~calendarID:%s ~~~~~~~~~~" %calendarId
    

FLOW = OAuth2WebServerFlow(
    client_id='154781234816-h5nmu0iuq3do0tsga33km22g2t0al0ru.apps.googleusercontent.com',
    client_secret='JRwb4_2ZXMe8iTf6t6GazJbD',
    scope='https://www.googleapis.com/auth/calendar.readonly',
    user_agent='test/0.1')

storage = Storage('../calendar.dat')
credentials = storage.get()
if credentials is None or credentials.invalid == True:
  credentials = run(FLOW, storage)

# Create an httplib2.Http object to handle our HTTP requests and authorize it
# with our good Credentials.
http = httplib2.Http()
http = credentials.authorize(http)

# Build a service object for interacting with the API. Visit
# the Google Developers Console
# to get a developerKey for your own application.
service = build(serviceName='calendar', version='v3', http=http,
                   developerKey='AIzaSyAv11A9OptZ5TX-Pqr3egpbddHQzQ_yULU')

# open database
db = MySQLdb.connect(host="localhost",user="ft",passwd="",db="foodtrucks", cursorclass=MySQLdb.cursors.DictCursor, sql_mode="STRICT_ALL_TABLES")

c = db.cursor()
#calendarId = 'rh7m6jon48l6ta3c1lpisle820@group.calendar.google.com'

#startTime = datetime.strptime(args.startDay, "20%y-%m-%d").isoformat()+"-0700"

# from today
startTime = datetime.now().replace(microsecond=0).isoformat()+"-0700"

# endTime is 7 days later than startTme  not include
endTime = (datetime.now().replace(microsecond=0) + timedelta(days=7)).isoformat()+"-0700"
#endTime = (datetime.strptime(args.startDay, "20%y-%m-%d") + timedelta(days=7)).isoformat()+"-0700"

name = args.name

page_token = None

sql = "INSERT INTO `schedules`(`date`, `name`, `meal`, `start_time`, `end_time`, `address`) VALUES (%s,%s,%s,%s,%s,%s)"


while True:
    events = service.events().list(calendarId=calendarId, pageToken=page_token, timeMin=startTime, timeMax=endTime, singleEvents=True).execute()
    for event in events['items']:
        total = total + 1
        if any(skip_event in event['summary'].lower() for skip_event in skip_events):
            print "----skip----"
            print event['summary']
            skip = skip + 1
            continue;
        try: 
            start_date = datetime.strptime(event['start']['dateTime'], "20%y-%m-%dT%H:%M:%S-07:00")
            end_date = datetime.strptime(event['end']['dateTime'], "20%y-%m-%dT%H:%M:%S-07:00")
            date = start_date.strftime("20%y-%m-%d")
            start_time = start_date.strftime("%H:%M:00")
            end_time = end_date.strftime("%H:%M:00")
            
            try: address = event['location']
            except: address = event['summary']

            if start_date.time() < time(14):
                meal = "lunch"
            else:
                meal = "dinner"

            sql_args = (date, name, meal, start_time, end_time, address)
            c.execute(sql, sql_args)
            success = success + 1
            print "~~~~added one event~~~~~~~"
            print sql_args
        except Exception, e: 
            fail = fail + 1
            print "~~~~~~ERROR:sql fail~~~~~~~"
            print "e: %s" %  e
            print event
            print "sql: %s" %sql
            print "start_date %s " %start_date
            print "end_date: %s" %end_date
            print "date:%s" %date
            print "start_time: %s" % start_time
            print "end_time: %s" %end_time
            print "address: %s" %address
            print "meal: %s" %meal
    page_token = events.get('nextPageToken')
    if not page_token:
        break

finish(args = args, total = total, fail = fail, skip = skip, success=success)
