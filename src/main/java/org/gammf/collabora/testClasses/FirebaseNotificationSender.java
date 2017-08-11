package org.gammf.collabora.testClasses;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class FirebaseNotificationSender {

    private static final String FIREBASE_URI = "https://fcm.googleapis.com/fcm/send";
    private static final String AUTHORIZATION = "AAAAJtSw2Gk:APA91bEXmB5sRFqSnuYIP3qofHQ0RfHrAzTllJ0vYWtHXKZsMdbuXmUKbr16BVZsMO0cMmm_BWE8oLzkFcyuMr_V6O6ilqvLu7TrOgirVES51Ux9PsKfJ17iOMvTF_WtwqEURqMGBbLf";
    private static final String NOTIFICATION_TITLE = "collabora-project";
    private static final String NOTIFICATION_BODY = "Peru inserted a note";
    private static final String TOPIC = "collabora-project-id";

    public static void main(String[] args) throws Exception {

        final HttpPost post = new HttpPost(FIREBASE_URI);
        post.setHeader("Authorization", "key=" + AUTHORIZATION);
        post.setHeader("Content-Type", "application/json");
        final String payload = "{\"notification\": { \"title\": \"" + NOTIFICATION_TITLE + "\", \"body\": \"" + NOTIFICATION_BODY + "\"}, \"to\" : \"/topics/" + TOPIC + "\"}";
        post.setEntity(new StringEntity(payload));

        final HttpClient http = HttpClientBuilder.create().build();
        final HttpResponse response = http.execute(post);
        System.out.println(response);
    }

}
