#!/usr/bin/env python 
from kafka import SimpleProducer, KafkaClient
import time

kafka = KafkaClient("localhost:9092")
producer = SimpleProducer(kafka)

content = None 
with open("twitter.json") as f:
  content = f.readlines()

for line in content:
  producer.send_messages("websocket", line)
  time.sleep(1)
