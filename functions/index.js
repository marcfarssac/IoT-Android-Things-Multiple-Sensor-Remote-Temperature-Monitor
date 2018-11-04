/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

// [START import]
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// [END import]

// [START helloWorld]
/**
 * Cloud Function to be triggered by Pub/Sub that logs a message using the data published to the
 * topic.
 */
// [START trigger]
exports.helloPubSub = functions.pubsub.topic('temp-reading').onPublish((message) => {
// [END trigger]
  // [START readBase64]
  // Decode the PubSub Message body.
  const messageBody = message.data ? Buffer.from(message.data, 'base64').toString() : null;

    let value = '';

    const value = '23.45';
    const sensorName = 'Room 1';
    const timeStamp = '12345678'

//    const value = messageBody.json.value;
//    const sensorName = messageBody.json.sensorName;
//    const timeStamp = messageBody.json.timestamp;

  // [END readBase64]
  // Print the message in the logs.
  console.log(`Timestamp is ${timeStamp} | Value is ${value} | SensorName is ${sensorName}`);


 // The topic name can be optionally prefixed with "/topics/".
 var topic = 'iot-temp';

 // See documentation on defining a message payload.
 var message = {
   data: {
     timeStamp: timeStamp,
     sensorName: sensorName,
     value: value
   },
   topic: topic
 };

 // Send a message to devices subscribed to the provided topic.
 admin.messaging().send(message)
   .then((response) => {
     // Response is a message ID string.
     console.log('Successfully sent message:', response);
   })
   .catch((error) => {
     console.log('Error sending message:', error);
   });

  return null;
});
// [END helloWorld]

/**
 * Cloud Function to be triggered by Pub/Sub that logs a message using the data published to the
 * topic as JSON.
 */
exports.helloPubSubJson = functions.pubsub.topic('another-topic-name').onPublish((message) => {
  // [START readJson]
  // Get the `name` attribute of the PubSub message JSON body.
  let name = null;
  try {
    name = message.json.name;
  } catch (e) {
    console.error('PubSub message was not JSON', e);
  }
  // [END readJson]
  // Print the message in the logs.
  console.log(`Hello ${name || 'World'}!`);
  return null;
});

/**
 * Cloud Function to be triggered by Pub/Sub that logs a message using the data attributes
 * published to the topic.
 */
exports.helloPubSubAttributes = functions.pubsub.topic('yet-another-topic-name').onPublish((message) => {
  // [START readAttributes]
  // Get the `name` attribute of the message.
  const name = message.attributes.name;
  // [END readAttributes]
  // Print the message in the logs.
  console.log(`Hello ${name || 'World'}!`);
  return null;
});
