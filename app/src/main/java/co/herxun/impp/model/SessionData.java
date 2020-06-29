package co.herxun.impp.model;

import java.io.Serializable;

public class SessionData implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String sessionId;
    private String groupId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

}
