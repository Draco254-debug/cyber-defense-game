import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameApp {
    private static PrintWriter out;
    private static BufferedReader in;
    private static JPanel telemetryGrid;
    private static JTextArea terminalConsole;

    // --- UPGRADE B: GLOBAL HUD UI TARGETS ---
    private static JProgressBar globalHealthBar;
    private static JLabel waveTrackerLabel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Cyber Operations Command Center - Operations HUD");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLayout(new BorderLayout());

        // --- UPGRADE B: HEADER STATS STATUS BAR ---
        JPanel headerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        headerPanel.setBackground(new Color(15, 15, 22));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        waveTrackerLabel = new JLabel("TACTICAL PHASE: WAVE 1 | ELAPSED TIME: 0s", SwingConstants.LEFT);
        waveTrackerLabel.setForeground(Color.CYAN);
        waveTrackerLabel.setFont(new Font("Courier New", Font.BOLD, 14));

        globalHealthBar = new JProgressBar(0, 100);
        globalHealthBar.setValue(100);
        globalHealthBar.setStringPainted(true);
        globalHealthBar.setString("TOTAL MAINFRAME INTEGRITY: 100%");
        globalHealthBar.setForeground(Color.GREEN);
        globalHealthBar.setBackground(Color.DARK_GRAY);
        globalHealthBar.setFont(new Font("Courier New", Font.BOLD, 12));

        headerPanel.add(waveTrackerLabel);
        headerPanel.add(globalHealthBar);
        frame.add(headerPanel, BorderLayout.NORTH);

        // --- LEFT COLUMN: TERMINAL AREA ---
        JPanel terminalPanel = new JPanel(new BorderLayout());
        terminalConsole = new JTextArea("============================================================\n" +
                "   CENTRAL CYBER DEFENSE TERMINAL ONLINE v2.5\n" +
                "============================================================\n" +
                "System Commands:\n - scan              (Audit system kernels/vulns)\n" +
                " - mitigate [IP]     (Deploy instant firewall patch)\n" +
                " - run [script.py]   (Execute an automation file)\n\nroot@ops-node:~# ");
        terminalConsole.setEditable(false);
        terminalConsole.setBackground(new Color(10, 10, 12));
        terminalConsole.setForeground(new Color(0, 255, 100));
        terminalConsole.setFont(new Font("Courier New", Font.PLAIN, 13));
        JScrollPane terminalScroll = new JScrollPane(terminalConsole);

        JTextField terminalInput = new JTextField(20);
        terminalInput.setPreferredSize(new Dimension(400, 38));
        terminalInput.setBackground(new Color(25, 25, 30));
        terminalInput.setForeground(new Color(0, 255, 100));
        terminalInput.setCaretColor(new Color(0, 255, 100));
        terminalInput.setFont(new Font("Courier New", Font.BOLD, 14));
        terminalInput.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 100)));

        terminalPanel.add(terminalScroll, BorderLayout.CENTER);
        terminalPanel.add(terminalInput, BorderLayout.SOUTH);

        // --- RIGHT COLUMN: VISUAL DASHBOARD ---
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(new Color(22, 22, 30));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel dashLabel = new JLabel("🛡️ LIVE SUBNET ACTIVITY MATRIX", SwingConstants.CENTER);
        dashLabel.setForeground(Color.WHITE);
        dashLabel.setFont(new Font("Courier New", Font.BOLD, 14));
        dashLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        telemetryGrid = new JPanel(new GridLayout(0, 1, 12, 12));
        telemetryGrid.setBackground(new Color(22, 22, 30));
        JScrollPane diagnosticScroll = new JScrollPane(telemetryGrid);
        diagnosticScroll.setBorder(null);

        dashboardPanel.add(dashLabel, BorderLayout.NORTH);
        dashboardPanel.add(diagnosticScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, terminalPanel, dashboardPanel);
        splitPane.setDividerLocation(520);
        frame.add(splitPane, BorderLayout.CENTER);

        // --- ADVANCED COMMAND CONTROLLER ---
        terminalInput.addActionListener(e -> {
            String input = terminalInput.getText().trim();
            if (input.isEmpty()) return;
            terminalConsole.append(input);

            if (input.equals("scan")) {
                sendCommandToPython("{\"command\": \"SCAN_SYSTEMS\"}");
                terminalConsole.append("\n[*] Initializing infrastructure audit sweep...");
            } else if (input.startsWith("mitigate ")) {
                String ip = input.replace("mitigate ", "").trim();
                sendCommandToPython(String.format("{\"command\": \"DEPLOY_FILTER\", \"target_ip\": \"%s\"}", ip));
                terminalConsole.append("\n[*] Pushing firewall rule signatures to node: " + ip);
            } else if (input.startsWith("run ")) {
                String file = input.replace("run ", "").trim();
                sendCommandToPython(String.format("{\"command\": \"RUN_SCRIPT\", \"script_file\": \"%s\"}", file));
                terminalConsole.append("\n[*] Spawning sandbox runtime sub-process for: " + file);
            } else {
                terminalConsole.append("\nCommand execution failure: Invalid tactical signature.");
            }
            terminalConsole.append("\nroot@ops-node:~# ");
            terminalInput.setText("");
        });

        frame.setVisible(true);
        terminalInput.requestFocusInWindow();

        connectToPythonBackend();

        // --- BACKGROUND LIVE POLL TIMER LOOP ---
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1500); // Ticks updates faster every 1.5 seconds
                    sendCommandToPython("{\"command\": \"GET_NETWORK_STATUS\"}");
                } catch (InterruptedException ex) { break; }
            }
        }).start();
    }

    // --- PARSING ENGINE AND DYNAMIC DASHBOARD GENERATOR ---
    private static void updateGraphicalDashboard(String json) {
        telemetryGrid.removeAll();

        // Parse global game metrics from the composite packet
        int integrity = Integer.parseInt(extractJsonValue(json, "integrity"));
        int wave = Integer.parseInt(extractJsonValue(json, "wave"));
        int clock = Integer.parseInt(extractJsonValue(json, "clock"));

        // Update overall HUD state elements dynamically
        globalHealthBar.setValue(integrity);
        globalHealthBar.setString("TOTAL MAINFRAME INTEGRITY: " + integrity + "%");
        waveTrackerLabel.setText(String.format("TACTICAL PHASE: WAVE %d | ACTIVE RUNTIME: %ds", wave, clock));

        // Color transition logic for global security levels
        if (integrity > 70) globalHealthBar.setForeground(Color.GREEN);
        else if (integrity > 35) globalHealthBar.setForeground(Color.ORANGE);
        else globalHealthBar.setForeground(Color.RED);

        // Build out visual hardware cards per device node detected
        Pattern pattern = Pattern.compile("\"(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\":\\s*\\{[^\\}]+\\}");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String ip = matcher.group(1);
            String subBlock = matcher.group(0);

            String name = extractJsonValue(subBlock, "name");
            String status = extractJsonValue(subBlock, "status");
            int traffic = Integer.parseInt(extractJsonValue(subBlock, "traffic"));

            JPanel nodeRow = new JPanel(new BorderLayout());
            nodeRow.setBackground(new Color(35, 35, 48));
            nodeRow.setBorder(BorderFactory.createMatteBorder(1, 6, 1, 1, status.equals("SECURE") ? Color.GREEN : Color.RED));
            nodeRow.setPreferredSize(new Dimension(400, 65));

            JLabel infoLabel = new JLabel(String.format(" %s (%s) - %s", name, ip, status));
            infoLabel.setFont(new Font("Courier New", Font.BOLD, 12));
            infoLabel.setForeground(status.equals("SECURE") ? new Color(50, 220, 50) : Color.RED);

            JProgressBar trafficBar = new JProgressBar(0, 100);
            trafficBar.setValue(traffic);
            trafficBar.setStringPainted(true);
            trafficBar.setString("Network Port Load: " + traffic + "%");

            // Internal color logic for traffic metrics
            if (traffic > 80) trafficBar.setForeground(Color.RED);
            else if (traffic > 50) trafficBar.setForeground(Color.ORANGE);
            else trafficBar.setForeground(Color.GREEN);
            trafficBar.setBackground(Color.DARK_GRAY);

            nodeRow.add(infoLabel, BorderLayout.NORTH);
            nodeRow.add(trafficBar, BorderLayout.CENTER);
            telemetryGrid.add(nodeRow);
        }
        telemetryGrid.revalidate();
        telemetryGrid.repaint();
    }

    private static String extractJsonValue(String block, String key) {
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\"?([^\",\\}]+)\"?");
        Matcher m = p.matcher(block);
        return m.find() ? m.group(1).trim() : "0";
    }

    private static void connectToPythonBackend() {
        new Thread(() -> {
            try {
                Socket socket = new Socket("127.0.0.1", 9999);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String response;
                while ((response = in.readLine()) != null) {
                    final String rawJson = response;

                    if (rawJson.contains("\"nodes\":")) {
                        SwingUtilities.invokeLater(() -> updateGraphicalDashboard(rawJson));

                        if (rawJson.contains("\"narrative\":")) {
                            String storyMessage = extractJsonValue(rawJson, "narrative");
                            SwingUtilities.invokeLater(() -> {
                                terminalConsole.append("\n\n" + storyMessage + "\n\nroot@ops-node:~# ");
                            });
                        }
                    } else if (rawJson.contains("\"message\":")) {
                        String msg = extractJsonValue(rawJson, "message").replace("\\n", "\n");
                        SwingUtilities.invokeLater(() -> {
                            terminalConsole.append("\n" + msg + "\nroot@ops-node:~# ");
                        });
                    }
                }
            } catch (IOException e) {
                System.out.println("Socket pipeline dropped.");
            }
        }).start();
    }

    private static void sendCommandToPython(String jsonMessage) {
        if (out != null) {
            out.println(jsonMessage);
        }
    }
} // <-- This final brace closes the entire GameApp class perfectly
