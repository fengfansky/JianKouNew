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
import com.activeandroid.query.Select;

@Table(name = "Community")
public class Community extends Model implements Serializable {
    @Column(name = "communityId")
    public String communityId;
    @Column(name = "userId")
    public String userId;
    @Column(name = "communityName")
    public String communityName;
    @Column(name = "communityDesc")
    public String communityDesc;
    @Column(name = "communityUrl")
    public String communityUrl;
    @Column(name = "communityPhotoUrl")
    public String communityPhotoUrl;
    @Column(name = "createdAt")
    public long createdAt;

    public Community() {
    }

    public Community(JSONObject json) {
        parseJSON(json);
    }

    public Community update() {
        Community communityExisit = new Select().from(Community.class).where("communityId = ? ", communityId)
                .executeSingle();

        // 不存在
        if (communityExisit == null) {
            communityExisit = this;
        } else {
            if (communityId != null)
                communityExisit.communityId = communityId;
            if (userId != null)
                communityExisit.userId = userId;
            if (communityName != null)
                communityExisit.communityName = communityName;
            if (communityDesc != null)
                communityExisit.communityDesc = communityDesc;
            if (communityUrl != null)
                communityExisit.communityUrl = communityUrl;
            if (communityPhotoUrl != null)
                communityExisit.communityPhotoUrl = communityPhotoUrl;
        }

        communityExisit.save();
        return communityExisit;
    }

    public Community getFromTable() {
        Community userExisit = new Select().from(Community.class).where("communityId = ? ", communityId)
                .executeSingle();
        return userExisit;
    }

    public void parseJSON(JSONObject json) {
        try {
            communityId = json.getString("id");
            communityName = json.getString("communityName");
            communityDesc = json.getString("communityDesc");
            communityUrl = json.getString("communityUrl");
            communityPhotoUrl = json.getString("communityPhotoUrl");

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            c.setTime(sdf.parse(json.getString("created_at")));
            createdAt = c.getTimeInMillis();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
