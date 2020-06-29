package co.herxun.impp.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "Event")
public class Event extends Model implements Serializable {
    @Column(name = "eventId")
    public String eventId;
    @Column(name = "userId")
    public String userId;
    @Column(name = "currentUserId")
    public String currentUserId;
    @Column(name = "eventTitle")
    public String eventTitle;
    @Column(name = "eventInformation")
    public String eventInformation;
    @Column(name = "startTime")
    public long startTime;
    @Column(name = "duration")
    public long duration;
    @Column(name = "likeCount")
    public int likeCount;
    @Column(name = "attendedUserIds")
    public String attendedUserIds;
    @Column(name = "eventPhotoUrl")
    public String eventPhotoUrl;
    @Column(name = "createdAt")
    public long createdAt;
    @Column(name = "eventCost")
    public String eventCost;
    @Column(name = "eventUserLimit")
    public int eventUserLimit;
    @Column(name = "address")
    public String address;
    @Column(name = "pageview")
    public int pageview;
    @Column(name = "isRead")
    public boolean isRead;
    @Column(name = "attendedAt")
    public long attendedAt;

    public Event() {
    }

    public Event(JSONObject json, String currentUserId) {
        parseJSON(json, currentUserId);
    }

    public Like myLike(User user) {
        user = user.getFromTable();
        return new Select().from(Like.class)
                .where("Event = \"" + getFromTable(currentUserId).getId() + "\" and Owner = \"" + user.getId() + "\"")
                .executeSingle();
    }

    public Event update() {
        Event eventExisit = new Select().from(Event.class)
                .where("eventId = ? and currentUserId = ? ", eventId, currentUserId).executeSingle();

        // 不存在
        if (eventExisit == null) {
            eventExisit = this;
        } else {
            if (eventId != null)
                eventExisit.eventId = eventId;
            if (userId != null)
                eventExisit.userId = userId;
            if (eventTitle != null)
                eventExisit.eventTitle = eventTitle;
            if (eventInformation != null)
                eventExisit.eventInformation = eventInformation;
            if (attendedUserIds != null)
                eventExisit.attendedUserIds = attendedUserIds;
            if (eventCost != null)
                eventExisit.eventCost = eventCost;
            if (address != null)
                eventExisit.address = address;
            if (pageview != 0)
                eventExisit.pageview = pageview;
            if (eventPhotoUrl != null)
                eventExisit.eventPhotoUrl = eventPhotoUrl;
            if (currentUserId != null)
                eventExisit.currentUserId = currentUserId;
            if (createdAt != 0l)
                eventExisit.createdAt = createdAt;
            if (startTime != 0l)
                eventExisit.startTime = startTime;
            if (duration != 0)
                eventExisit.duration = duration;
            if (likeCount != 0)
                eventExisit.likeCount = likeCount;
            if (eventUserLimit != 0)
                eventExisit.eventUserLimit = eventUserLimit;
            if (attendedAt != 0l)
                eventExisit.attendedAt = attendedAt;
        }

        eventExisit.save();
        return eventExisit;
    }

    public Event getFromTable(String currentUserId) {
        Event userExisit = new Select().from(Event.class)
                .where("eventId = ? and currentUserId = ? ", eventId, currentUserId).executeSingle();
        return userExisit;
    }

    public void parseJSON(JSONObject json, String currentUserId) {
        try {
            eventId = json.getString("id");
            eventTitle = json.getString("title");
            eventInformation = json.getString("information");
            duration = json.getLong("duration");
            likeCount = json.getInt("likeCount");
            if (json.has("customFields")) {
                if (json.getJSONObject("customFields").has("cost")) {
                    eventCost = json.getJSONObject("customFields").getString("cost");
                }
                if (json.getJSONObject("customFields").has("user_limit")) {
                    eventUserLimit = Integer.parseInt(json.getJSONObject("customFields").getString("user_limit"));
                }
                if (json.getJSONObject("customFields").has("address")) {
                    address = json.getJSONObject("customFields").getString("address");
                }
                if (json.getJSONObject("customFields").has("pageview")) {
                    pageview = Integer.parseInt(json.getJSONObject("customFields").getString("pageview"));
                }
                if (json.getJSONObject("customFields").has("photo_url")) {
                    eventPhotoUrl = json.getJSONObject("customFields").getString("photo_url");
                }
            }

            if (json.has("user")) {
                userId = json.getJSONObject("user").getString("id");
                JSONObject userJSON = json.getJSONObject("user");
                User user = new User();
                user.parseJSON(userJSON);
                user.update();
            }

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            c.setTime(sdf.parse(json.getString("created_at")));
            createdAt = c.getTimeInMillis();
            c.setTime(sdf.parse(json.getString("startTime")));
            startTime = c.getTimeInMillis();
            if (json.has("attended_at")) {
                c.setTime(sdf.parse(json.getString("attended_at")));
                attendedAt = c.getTimeInMillis();
            }

            this.currentUserId = currentUserId;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllLikes(String currentUserId) {
        new Delete().from(Like.class)
                .where("Event = ? and currentUserId = ?", getFromTable(currentUserId).getId(), currentUserId).execute();
    }

    public boolean isAttend(String currentUserId) {
        if (this.attendedUserIds != null) {
            return this.attendedUserIds.contains(currentUserId);
        } else {
            return false;
        }

    }
}
