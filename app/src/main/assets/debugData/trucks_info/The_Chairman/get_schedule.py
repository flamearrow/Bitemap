import urllib2
import re
import pdb
import MySQLdb
import MySQLdb.cursors 
import sys
import os

def convert_to_date(date_str):
    date_str = date_str.replace(' ','')
    match = re.compile(r"""(?P<hour>[0-9]{1,2})(:(?P<mins>[0-9]{1,2}))?(?P<time>[a-zA-Z]+)?""").match(date_str)

    if not match or not match.groupdict()['hour']:
        raise Exception("shit :%s" %date_str)

    hour =  int(match.groupdict()['hour'])
    if not match.groupdict()['mins']:
        mins = "00"
    else:
        mins = int(match.groupdict()['mins'])

    if match.groupdict()['time']:
        if match.groupdict()['time'] == "PM" and hour != 12:
            if hour >= 12:
                raise Exception ("hour : %s ??" % hour) 
            hour = hour + 12

    return "%s:%s:00" % (hour, mins)

url = "http://www.thechairmantruck.com/locate"

name = "The Chairman"

reponse = urllib2.urlopen(url)

html = reponse.read()

daily_schedule_pattern = re.compile(r'<div data-weekday=.*?<h3>(?P<month>[0-9]{2})/(?P<day>[0-9]{2})/(?P<year>[0-9]{4}) (?P<weekday>[A-Z]{3}).*?</h3>.*?             </div>.*?\n</div>', re.S)

iter = daily_schedule_pattern.finditer(html, re.S)

weekly = []

for day in iter:

    daily = {}
    daily['date'] = "%s-%s-%s" % (day.groupdict()['year'],
                                  day.groupdict()['month'], 
                                  day.groupdict()['day'])
    

    daily['schedules'] = []

    schedules = re.compile(r"""<div class="(?P<meal>((lunch)|(dinner)))_booking">.*?<address>(?P<address>.*?)[\n].*?<div class="hours">(?P<start_time>.*?)-(?P<end_time>.*?)</div>""", re.S)

    all_meals = schedules.finditer(day.group(0), re.S)
    for meal in all_meals:

        schedule = {}
        schedule['meal'] = meal.groupdict()['meal']
        schedule['start_time'] = convert_to_date(meal.groupdict()['start_time'])
        schedule['end_time'] = convert_to_date(meal.groupdict()['end_time'])
        schedule['address'] = meal.groupdict()['address']

        daily['schedules'].append(schedule);

    weekly.append(daily)


db = MySQLdb.connect(host="localhost",user="ft",passwd="",db="foodtrucks", cursorclass=MySQLdb.cursors.DictCursor, sql_mode="STRICT_ALL_TABLES")
    
c = db.cursor()

sql = """INSERT INTO `schedules`(`name`, `date`, `meal`, `start_time`, `end_time`, `address`) VALUES (%s, %s, %s, %s, %s, %s)"""

total = 0
fail = 0
for day in weekly:
    for schedule in day['schedules']:
        total = total + 1

        args = (name, day['date'], schedule['meal'], schedule['start_time'],schedule['end_time'], schedule['address'])

        try: c.execute(sql, args)
        except Exception, e: 
            fail = fail + 1
            print "~~~~~~error:~~~~~~~"
            print e
            print sql
            print args

print "~~~~~~~~~~DONE~~~~~~~~~~"
print "total: %s" % total
print "failed : %s" % fail
    


