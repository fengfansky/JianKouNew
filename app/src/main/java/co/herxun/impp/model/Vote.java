package co.herxun.impp.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "Vote")
public class Vote extends Model implements Serializable {
    @Column(name = "voteId")
    public String voteId;
    @Column(name = "userId")
    public String userId;
    @Column(name = "currentUserId")
    public String currentUserId;
    @Column(name = "voteTitle")
    public String voteTitle;
    @Column(name = "voteContent")
    public String voteContent;
    @Column(name = "voteChoices")
    public String voteChoices;
    @Column(name = "voteResults")
    public String voteResults;
    @Column(name = "voteSelectedChoices")
    public String voteSelectedChoices;
    @Column(name = "voteType")
    public String voteType;
    @Column(name = "votePhotoUrl")
    public String votePhotoUrl;
    @Column(name = "votedAt")
    public long votedAt;
    @Column(name = "createdAt")
    public long createdAt;
    @Column(name = "votedUserCount")
    public int votedUserCount;

    public Vote() {
    }

    public Vote(JSONObject json, String currentUserId) {
        parseJSON(json, currentUserId);
    }

    public Vote update() {
        Vote voteExisit = new Select().from(Vote.class)
                .where("voteId = ? and currentUserId = ? ", voteId, currentUserId).executeSingle();

        // 不存在
        if (voteExisit == null) {
            voteExisit = this;
        } else {
            if (voteId != null)
                voteExisit.voteId = voteId;
            if (userId != null)
                voteExisit.userId = userId;
            if (voteTitle != null)
                voteExisit.voteTitle = voteTitle;
            if (voteContent != null)
                voteExisit.voteContent = voteContent;
            if (voteChoices != null)
                voteExisit.voteChoices = voteChoices;
            if (voteResults != null)
                voteExisit.voteResults = voteResults;
            if (voteSelectedChoices != null)
                voteExisit.voteSelectedChoices = voteSelectedChoices;
            if (voteType != null)
                voteExisit.voteType = voteType;
            if (votePhotoUrl != null)
                voteExisit.votePhotoUrl = votePhotoUrl;
            if (votedAt != 0l)
                voteExisit.votedAt = votedAt;
            if (currentUserId != null)
                voteExisit.currentUserId = currentUserId;
            if (createdAt != 0l)
                voteExisit.createdAt = createdAt;
            if (votedUserCount != 0) {
                voteExisit.votedUserCount = votedUserCount;
            }
        }

        voteExisit.save();
        return voteExisit;
    }

    public Vote getFromTable() {
        Vote userExisit = new Select().from(Vote.class).where("voteId = ? ", voteId).executeSingle();
        return userExisit;
    }

    public void parseJSON(JSONObject json, String currentUserId) {
        try {
            voteId = json.getString("id");
            voteTitle = json.getString("title");
            voteContent = json.getString("content");
            if (json.has("votedUserCount")) {
                votedUserCount = json.getInt("votedUserCount");
            } else {
                votedUserCount = 0;
            }
            JSONObject choicesJson = json.getJSONObject("choices");
            String choices = "";
            Iterator<String> it = choicesJson.keys();
            String key = "";
            String value = "";
            while (it.hasNext()) {
                key = (String) it.next();
                value = choicesJson.getString(key);
                choices = choices + key + ":" + value + ",";
            }
            if (choices.length() > 0) {
                choices = choices.substring(0, choices.length() - 1);
            }
            voteChoices = choices;

            JSONObject voteResultsJson = json.getJSONObject("results");
            String results = "";
            it = choicesJson.keys();
            while (it.hasNext()) {
                key = (String) it.next();
                value = voteResultsJson.getString(key);
                results = results + key + ":" + value + ",";
            }
            if (results.length() > 0) {
                results = results.substring(0, results.length() - 1);
            }
            voteResults = results;

            if (json.has("selected_choices")) {
                JSONArray selectedChoicesArray = json.getJSONArray("selected_choices");
                String selectedChoices = "";
                for (int i = 0; i < selectedChoicesArray.length(); i++) {
                    selectedChoices = selectedChoices + selectedChoicesArray.getString(i) + ",";
                }
                if (selectedChoices.length() > 0) {
                    selectedChoices = selectedChoices.substring(0, selectedChoices.length() - 1);
                }
                voteSelectedChoices = selectedChoices;
            }

            if (json.has("customFields")) {
                if (json.getJSONObject("customFields").has("voteType")) {
                    voteType = json.getJSONObject("customFields").getString("voteType");
                }
                if (json.getJSONObject("customFields").has("votePhotoUrl")) {
                    votePhotoUrl = json.getJSONObject("customFields").getString("votePhotoUrl");
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
            if (json.has("voted_at")) {
                c.setTime(sdf.parse(json.getString("voted_at")));
                votedAt = c.getTimeInMillis();
            }

            this.currentUserId = currentUserId;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean isVoted() {
        return this.votedAt != 0l;
    }
}
