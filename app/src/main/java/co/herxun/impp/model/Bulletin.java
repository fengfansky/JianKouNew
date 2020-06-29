package co.herxun.impp.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "Bulletin")
public class Bulletin extends Model implements Serializable {
    @Column(name = "bulletinId")
    public String bulletinId;
    @Column(name = "userId")
    public String userId;
    @Column(name = "bulletinTitle")
    public String bulletinTitle;
    @Column(name = "bulletinContent")
    public String bulletinContent;
    @Column(name = "bulletinType")
    public String bulletinType;
    @Column(name = "bullectinDescription")
    public String bullectinDescription;
    @Column(name = "bulletinCoverUrl")
    public String bulletinCoverUrl;
    @Column(name = "createdAt")
    public long createdAt;
    @Column(name = "pageview")
    public int pageview;
    @Column(name = "likeCount")
    public int likeCount;
    @Column(name = "currentUserId")
    public String currentUserId;
    @Column(name = "isRead")
    public boolean isRead;
    @Column(name = "state")
    public int state;

    public Bulletin() {
    }

    public Bulletin(JSONObject json, String currentUserId) {
        parseJSON(json, currentUserId);
    }

    public Bulletin update() {
        Bulletin bulletinExisit = new Select().from(Bulletin.class)
                .where("bulletinId = ? and currentUserId = ? ", bulletinId, currentUserId).executeSingle();

        // 不存在
        if (bulletinExisit == null) {
            bulletinExisit = this;
        } else {
            if (bulletinId != null)
                bulletinExisit.bulletinId = bulletinId;
            if (userId != null)
                bulletinExisit.userId = userId;
            if (currentUserId != null)
                bulletinExisit.currentUserId = currentUserId;
            if (bulletinTitle != null)
                bulletinExisit.bulletinTitle = bulletinTitle;
            if (bulletinContent != null)
                bulletinExisit.bulletinContent = bulletinContent;
            if (bullectinDescription != null)
                bulletinExisit.bullectinDescription = bullectinDescription;
            if (bulletinType != null)
                bulletinExisit.bulletinType = bulletinType;
            if (bulletinCoverUrl != null)
                bulletinExisit.bulletinCoverUrl = bulletinCoverUrl;
            if (likeCount != 0)
                bulletinExisit.likeCount = likeCount;
            if (pageview != 0)
                bulletinExisit.pageview = pageview;
            if (createdAt != 0l)
                bulletinExisit.createdAt = createdAt;
            if (isRead)
                bulletinExisit.isRead = isRead;
            if (state != 0)
                bulletinExisit.state = state;
        }

        bulletinExisit.save();
        return bulletinExisit;
    }

    public Bulletin getFromTable(String currentUserid) {
        Bulletin userExisit = new Select().from(Bulletin.class)
                .where("bulletinId = ? and currentUserId = ? ", bulletinId, currentUserId).executeSingle();
        return userExisit;
    }

    public void parseJSON(JSONObject json, String currentUserId) {
        try {
            bulletinId = json.getString("id");
            bulletinTitle = json.getString("title");
            bulletinContent = json.getString("content");
            bulletinType = json.getString("type");
            likeCount = json.getInt("likeCount");
            JSONArray imageIds = json.getJSONArray("imageIds");
            if (imageIds.length() > 0) {
                bulletinCoverUrl = imageIds.getJSONObject(0).getString("url");
            }
            if (json.has("customFields")) {
                if (json.getJSONObject("customFields").has("pageview")) {
                    pageview = json.getJSONObject("customFields").getInt("pageview");
                }
                if (json.getJSONObject("customFields").has("bulletin_description")) {
                    bullectinDescription = json.getJSONObject("customFields").getString("bulletin_description");
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

            this.currentUserId = currentUserId;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllLikes(String currentUserId) {
        new Delete().from(Like.class)
                .where("Bulletin = ? and currentUserId = ?", getFromTable(currentUserId).getId(), currentUserId)
                .execute();
    }

    public Like myLike(User user) {
        user = user.getFromTable();
        return new Select()
                .from(Like.class)
                .where("Bulletin = \"" + getFromTable(currentUserId).getId() + "\" and Owner = \"" + user.getId()
                        + "\"").executeSingle();
    }
}
