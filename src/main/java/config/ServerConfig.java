package config;

public record ServerConfig(int port, String dir, String dbfilename) {
    public static ServerConfig fromArgs(String[] args){
        int port = 6379;
        String dir = "";
        String dbfilename = "";

        for(int i=0; i<args.length; i++){
            switch (args[i]) {
                case "--port" -> port = Integer.parseInt(args[++i]);
                case "--dir"  -> dir = args[++i];
                case "--dbfilename" -> dbfilename = args[++i];
            }
        }

        return new ServerConfig(port, dir, dbfilename);
    }
}
