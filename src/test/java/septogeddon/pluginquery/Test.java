package septogeddon.pluginquery;

public class Test {
    public static void main(String[]args) {
        QueryCompletableFuture<String> test = new QueryCompletableFuture<>();
        test.thenAccept(a -> {
            System.out.println(a);
        });
        test.complete("TEST TEST");
        test.thenAccept(a -> {
            System.out.println(a);
        });
    }
}
