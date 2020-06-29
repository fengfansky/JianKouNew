package co.herxun.impp.im.model;

import java.io.Serializable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "DeskGroup")
public class DeskGroup extends Model implements Serializable {
    @Column(name = "groupId")
    public String groupId;
    @Column(name = "groupName")
    public String groupName;
    @Column(name = "groupPhotoUrl")
    public String groupPhotoUrl;
    @Column(name = "groupSessionId")
    public String groupSessionId;
    @Column(name = "groupAccountId")
    public String groupAccountId;
    @Column(name = "groupAccountName")
    public String groupAccountName;
    @Column(name = "currentClientId")
    public String currentClientId;

    public DeskGroup update(String currentClientId) {
        DeskGroup userExisit = new Select().from(DeskGroup.class)
                .where("groupId = ? and currentClientId = ? ", groupId, currentClientId).executeSingle();
        // 不存在
        if (userExisit == null) {
            save();
            return this;
        } else {
            if (groupName != null) {
                userExisit.groupName = groupName;
            }
            if (groupPhotoUrl != null) {
                userExisit.groupPhotoUrl = groupPhotoUrl;
            }
            if (groupSessionId != null) {
                userExisit.groupSessionId = groupSessionId;
            }
            if (groupAccountId != null) {
                userExisit.groupAccountId = groupAccountId;
            }
            if (groupAccountName != null) {
                userExisit.groupAccountName = groupAccountName;
            }
            if (currentClientId != null) {
                userExisit.currentClientId = currentClientId;
            }
            userExisit.save();
            return userExisit;
        }
    }

    public DeskGroup getFromTable(String currentClientId) {
        return new Select().from(DeskGroup.class)
                .where("groupId = ? and currentClientId = ? ", groupId, currentClientId).executeSingle();
    }

}
