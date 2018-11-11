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

exports.startDbPubSub = functions.pubsub.topic('temp-reading').onPublish((message) => {
  const messageBody = message.data ? Buffer.from(message.data, 'base64').toString() : null;
 var topic = 'iot-temp';

 let telemetryData = null;
  try {
    telemetryData = message.json.data;
  } catch (e) {
    console.error('PubSub message was not JSON', e);
  }

 const deviceId = message.attributes.deviceId;
 const deviceNumId = message.attributes.deviceNumId;
 const deviceRegistryId = message.attributes.deviceRegistryId;

 admin.database().ref('/messages').push({
  telemetryData: telemetryData,
  deviceId: deviceId,
  deviceNumId: deviceNumId,
  deviceRegistryId: deviceRegistryId,
  }).then(() => {
  console.log('New Message written to dB', message);
  // Returning the sanitized message to the client.
	})
    .catch((error) => {
     console.log('Error writing Text message to db:', error);
   });

  return null;
});
// [END helloWorld]