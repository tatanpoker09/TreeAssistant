public class MainTest {

    public static void main(String[] args) throws PorcupineManagerException {
        System.out.println(System.getProperty("java.library.path"));
        final String modelFilePath = "D:\\Programming\\TreeVoiceAssistant\\Porcupine-master\\lib\\common\\porcupine_params.pv"; // It is available at lib/common/porcupine_params.pv
        final String keywordFilePath = "D:\\Programming\\TreeVoiceAssistant\\Porcupine-master\\resources\\keyword_files\\alexa_windows.ppn";
        final float sensitivity = 0.5f;

        PorcupineManager manager = new PorcupineManager(
                modelFilePath,
                keywordFilePath,
                sensitivity,
                new KeywordCallback() {
                    @Override
                    public void run(int x) {
                        System.out.println("Detected!");
                    }
                });
        System.out.println("Starting porcupine...");
        manager.start();

    }
}
