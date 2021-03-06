# initialize tag information
# 1) basic value for hot and thrending
# 2) give channel tags

import urllib, urllib2
import os
from array import *
import MySQLdb
import time
import string
import codecs

dbcontent = MySQLdb.connect (host = "localhost",
                             user = "root",
                             passwd = "",
                             charset = "utf8",
                             use_unicode = True,
                             db = "nncloudtv_content")
contentCursor = dbcontent.cursor()
contentCursor.execute("""truncate tag """)               
contentCursor.execute("""truncate tag_map """) 
contentCursor.execute("""truncate category_map """)
contentCursor.execute("""insert into tag (name, updateDate) values ('hot(9x9en)', now()); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (1, 1); """)         
contentCursor.execute("""insert into tag_map (tagId, channelId) values (1, 2); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (1, 3); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (1, 4); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (1, 5); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (1, 6); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (1, 7); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (1, 8); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (1, 9); """)                                           
contentCursor.execute("""insert into tag (name, updateDate) values ('hot(9x9zh)', now()); """)  
contentCursor.execute("""insert into tag_map (tagId, channelId) values (2, 10); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (2, 11); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (2, 12); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (2, 13); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (2, 14); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (2, 15); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (2, 16); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (2, 17); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (2, 18); """)
contentCursor.execute("""insert into tag (name, updateDate) values ('trending(9x9en)', now()); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (3, 19); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (3, 20); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (3, 21); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (3, 22); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (3, 23); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (3, 24); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (3, 25); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (3, 26); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (3, 27); """)
contentCursor.execute("""insert into tag (name, updateDate) values ('trending(9x9zh)', now()); """) 
contentCursor.execute("""insert into tag_map (tagId, channelId) values (4, 28); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (4, 29); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (4, 30); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (4, 31); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (4, 32); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (4, 33); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (4, 34); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (4, 35); """)
contentCursor.execute("""insert into tag_map (tagId, channelId) values (4, 36); """)
#hot english from thrending
contentCursor.execute("""delete from counter_shard where id > 1 ; """)
contentCursor.execute("""ALTER TABLE counter_shard AUTO_INCREMENT = 2; ; """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch5058', 1); """)      
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch3722', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch116', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch928', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch3606', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch3811', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch3610', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch1992', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch3487', 1); """)
#hot chinese from trending
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch144', 1); """)           
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch229', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch185', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch538', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch516', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch512', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch349', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch37', 1); """)
contentCursor.execute("""insert into counter_shard (shardNumber, counterName, count) values (0,'ch507', 1); """)

current = "en"        
fileName = "contentPool_" + current + ".txt"
feed = open(fileName, "rU")
allCategoryId = 1 
if current == "zh":
   allCategoryId = 20
i = 0
for line in feed:  
  line = line.decode("utf-8-sig")
  i = i+1
  data = line.split('\t')
  cId = data[0].rstrip().lstrip()   
  cName = data[1].rstrip().lstrip()
  categoryName = data[3].rstrip().lstrip()
  tags = data[4].rstrip("\"").lstrip("\"")
  tag = tags.split(',')
  lang = data[5].rstrip().lstrip()
  sphere = data[6].rstrip().lstrip()  
  lang = lang.lower()
  sphere = sphere.lower()  
  if lang == 'tw':
     lang = 'zh'
  if lang == 'others':
     lang = 'other'
  if sphere == 'tw':
     sphere = 'zh'
  if sphere == 'worldwide':
     sphere = 'other'
  if sphere == 'us':
     sphere = 'en'
  print "i: " + str(i) + ";cid: " + str(cId) + "; name:" + cName + "; categoryName: " + categoryName + "; lang:" + lang + "; sphere:" + sphere + "; tags:" + tags   
  contentCursor.execute ("""             
     select id from nnchannel 
      where id = %s 
       """, (cId))        
  cnt = contentCursor.rowcount
  if cnt == 0:
     print "this cid does not exist:" + str(cId)

  ### update channel metadata               
  contentCursor.execute ("""             
		 update nnchannel set name = %s, lang = %s, sphere = %s, tag = %s
		  where id = %s
     """, (cName, lang, sphere, tags, cId)) 
  dbcontent.commit()
  ### update category info                
  print "categoryName: " + categoryName
  contentCursor.execute("""
    select id 
      from category
     where name = %s
     """, (categoryName))
  categoryRow = contentCursor.fetchone()
  categoryId = categoryRow[0]
  print "categoryId:" + str(categoryId)       
  print "channelId:" + str(cId)
  #contentCursor.execute("""
  #   select id
  #    from category_map
  #   where categoryId = %s and channelId = %s
  #   """, (categoryId, cId))
  #count = contentCursor.rowcount          
  #if count == 0:           
  contentCursor.execute("""
     insert into category_map (categoryId, channelId, updateDate)
                       values (%s, %s, now()) 
     """, (categoryId, cId))
  contentCursor.execute("""
     insert into category_map (categoryId, channelId, updateDate)
                       values (%s, %s, now()) 
     """, (allCategoryId, cId))
    
  if sphere == 'other':
     otherCategoryId = 0
     otherAllCategoryId = 0
     if current == "en":
        otherCategoryId = categoryId + 19
        otherAllCategoryId = 20
     if current == "zh":
        otherCategoryId = categoryId - 19
        otherAllCategoryId = 1
     contentCursor.execute("""
        insert into category_map (categoryId, channelId, updateDate)
                          values (%s, %s, now()) 
        """, (otherCategoryId, cId))     
     contentCursor.execute("""
        insert into category_map (categoryId, channelId, updateDate)
                          values (%s, %s, now()) 
        """, (otherAllCategoryId, cId))
  
  ### update tag info
  for t in tag:    
     t = t.lower()  
     t = t.rstrip().lstrip().lstrip("\t")
     print (t)
     tagId = 0
     if t != "":
        contentCursor.execute("""
          select id 
            from tag
           where name = %s
           """, (t))
        count = contentCursor.rowcount
        if count == 0:   
           contentCursor.execute("""
              insert into tag (name, updateDate)
                       values (%s, now()) 
              """, (t))                
           contentCursor.execute("""         
             select id 
               from tag
              where name = %s
              """, (t))
        tagRow = contentCursor.fetchone()
        tagId = tagRow[0]        
        contentCursor.execute("""
          select id
            from tag_map                          
           where tagId = %s and channelId = %s
           """, (tagId, cId))
        count = contentCursor.rowcount
        if count == 0:           
           contentCursor.execute("""
              insert into tag_map (tagId, channelId, updateDate)
                           values (%s, %s, now()) 
              """, (tagId, cId))               
  dbcontent.commit()      
  #if i > 2:                              
  #   break

print "record done:" + str(i)
feed.close()
dbcontent.close()
