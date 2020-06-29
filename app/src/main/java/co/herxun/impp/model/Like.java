package co.herxun.impp.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "Like")
public class Like extends Model {
    @Column(name = "likeId")
    public String likeId;

    @Column(name = "Owner")
    public User owner;

    @Column(name = "Post")
    public Post post;

    @Column(name = "Bulletin")
    public Bulletin bulletin;

    @Column(name = "Event")
    public Event event;

    @Column(name = "currentUserId")
    public String currentUserId;

    public void parseJSON(JSONObject json, String currentUserId) {
        try {
            likeId = json.getString("id");

            User user = new User();
            user.parseJSON(json.getJSONObject("user"));
            user = user.update();
            owner = user;

            this.currentUserId = currentUserId;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parseJSON(JSONObject json, User user, String currentUserId) {
        try {
            likeId = json.getString("id");
            owner = user;
            this.currentUserId = currentUserId;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean update() {
        Like exisit = new Select().from(Like.class).where("likeId = ?", likeId).executeSingle();

        // 不存在
        if (exisit == null) {
            save();
            return true;
        } else {
            return false;
        }
    }
}
