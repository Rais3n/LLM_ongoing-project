package pl.clockworkjava;

public final class AppConfig {

    public static final String CHAT_COMPLETIONS_URL = "https://router.huggingface.co/v1/chat/completions";
    public static final String EMBEDDINGS_URL = "https://router.huggingface.co/scaleway/v1/embeddings";
    public static final String DEFAULT_CHAT_MODEL = "meta-llama/Llama-3.1-8B-Instruct:novita";
    public static final String DEFAULT_EMBEDDING_MODEL = "qwen3-embedding-8b";
    public static final String QDRANT_BASE_URL = "http://localhost:6333";
    public static final String GOOGLE_TASKS_APPLICATION_NAME = "Clockwork Java Assistant";
    public static final String GOOGLE_CREDENTIALS_PATH = "credentials.json";

    private AppConfig() {
    }

    public static String getHfToken() {
        return System.getenv("HF_TOKEN");
    }

    public static String getEmailPassword() {
        return System.getenv("GMAIL_APP_PASSWORD");
    }

    public static String getEmailUsername() {
        return System.getenv("EMAIL_USERNAME");
    }
}
