package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals;

public class BaseMessage {
    private String role;
    private String content;

    public BaseMessage() {

    }

    public BaseMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
