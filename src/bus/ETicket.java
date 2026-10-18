package bus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ETicket extends JFrame {

        private static final Color NAVY = new Color(15, 29, 55);
        private static final Color BLUE = new Color(42, 104, 210);
        private static final Color LIGHT_BG = new Color(235, 242, 252);
        private static final Color TEXT = new Color(45, 55, 72);
        private static final Color MUTED = new Color(110, 122, 142);

        private final JPanel ticketPanel;
        private final String busId;
        private final String passenger;
        private final String seats;
        private final String journeyDate;
        private final String departure;
        private final String arrival;
        private final String payment;
        private final double totalAmount;

        public ETicket(String busId, String passenger, String seats,
                        String journeyDate, String departure, String arrival,
                        String payment, double totalAmount) {

                this.busId = busId;
                this.passenger = passenger;
                this.seats = seats;
                this.journeyDate = journeyDate;
                this.departure = departure;
                this.arrival = arrival;
                this.payment = payment;
                this.totalAmount = totalAmount;

                setTitle("BusGo | E-Ticket");
                setSize(620, 750);
                setMinimumSize(new Dimension(560, 700));
                setLocationRelativeTo(null);
                setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JPanel background = new JPanel(new GridBagLayout());
                background.setBackground(LIGHT_BG);

                ticketPanel = new JPanel(new BorderLayout());
                ticketPanel.setBackground(Color.WHITE);
                ticketPanel.setBorder(BorderFactory.createLineBorder(
                                new Color(220, 227, 238)));

                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(NAVY);
                header.setBorder(new EmptyBorder(24, 30, 24, 30));

                JPanel brandBlock = new JPanel();
                brandBlock.setOpaque(false);
                brandBlock.setLayout(new BoxLayout(brandBlock, BoxLayout.Y_AXIS));

                JLabel brand = new JLabel("BusGo");
                brand.setFont(new Font("Segoe UI", Font.BOLD, 30));
                brand.setForeground(Color.WHITE);

                JLabel heading = new JLabel("E-TICKET  |  BOOKING CONFIRMED");
                heading.setFont(new Font("Segoe UI", Font.BOLD, 13));
                heading.setForeground(new Color(205, 220, 240));

                brandBlock.add(brand);
                brandBlock.add(Box.createVerticalStrut(5));
                brandBlock.add(heading);
                header.add(brandBlock, BorderLayout.WEST);

                JLabel confirmed = new JLabel("CONFIRMED");
                confirmed.setFont(new Font("Segoe UI", Font.BOLD, 12));
                confirmed.setForeground(new Color(190, 245, 214));
                confirmed.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(80, 150, 130)),
                                new EmptyBorder(8, 10, 8, 10)));
                header.add(confirmed, BorderLayout.EAST);

                JPanel journey = new JPanel(new GridLayout(1, 3, 12, 0));
                journey.setBackground(LIGHT_BG);
                journey.setBorder(new EmptyBorder(20, 25, 20, 25));
                journey.add(makeInfoBlock("DEPARTURE", departure));
                journey.add(makeInfoBlock("JOURNEY DATE", journeyDate));
                journey.add(makeInfoBlock("ARRIVAL", arrival));

                JPanel details = new JPanel(new GridLayout(0, 2, 18, 16));
                details.setBackground(Color.WHITE);
                details.setBorder(new EmptyBorder(25, 30, 25, 30));

                addDetail(details, "PASSENGER", passenger);
                addDetail(details, "BUS ID", busId);
                addDetail(details, "SEAT NUMBER(S)", seats);
                addDetail(details, "PAYMENT METHOD", payment);

                JPanel farePanel = new JPanel(new BorderLayout());
                farePanel.setBackground(new Color(235, 242, 255));
                farePanel.setBorder(new EmptyBorder(16, 30, 16, 30));

                JLabel fareLabel = new JLabel("TOTAL FARE");
                fareLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                fareLabel.setForeground(MUTED);

                JLabel fareValue = new JLabel(String.format(
                                java.util.Locale.US, "Rs. %.2f", totalAmount));
                fareValue.setFont(new Font("Segoe UI", Font.BOLD, 25));
                fareValue.setForeground(BLUE);

                farePanel.add(fareLabel, BorderLayout.WEST);
                farePanel.add(fareValue, BorderLayout.EAST);

                JLabel note = new JLabel(
                                "Please keep this ticket available during your journey.");
                note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                note.setForeground(MUTED);
                note.setBorder(new EmptyBorder(14, 30, 14, 30));

                JPanel center = new JPanel(new BorderLayout());
                center.setBackground(Color.WHITE);
                center.add(journey, BorderLayout.NORTH);
                center.add(details, BorderLayout.CENTER);
                center.add(farePanel, BorderLayout.SOUTH);

                JPanel bottom = new JPanel(new BorderLayout());
                bottom.setBackground(Color.WHITE);
                bottom.add(note, BorderLayout.CENTER);

                // Only one button.
                JPanel actions = new JPanel(
                                new FlowLayout(FlowLayout.RIGHT, 12, 14));
                actions.setBackground(Color.WHITE);
                actions.setBorder(new EmptyBorder(0, 20, 5, 20));

                JButton saveButton = new JButton("Save Ticket");
                stylePrimaryButton(saveButton);
                saveButton.addActionListener(e -> saveTicketAsPdf());

                actions.add(saveButton);
                bottom.add(actions, BorderLayout.SOUTH);
                actions.setVisible(true);
                bottom.setVisible(true);

                ticketPanel.add(header, BorderLayout.NORTH);
                ticketPanel.add(center, BorderLayout.CENTER);
                ticketPanel.add(bottom, BorderLayout.SOUTH);

                // Faint BusGo watermark behind the ticket.
                JPanel watermarkPanel = new JPanel(new GridBagLayout()) {
                        @Override
                        protected void paintComponent(Graphics graphics) {
                                super.paintComponent(graphics);

                                Graphics2D g2 = (Graphics2D) graphics.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);

                                g2.setColor(new Color(42, 104, 210, 35));
                                g2.setFont(new Font("Segoe UI", Font.BOLD, 76));

                                FontMetrics fm = g2.getFontMetrics();
                                String watermark = "BusGo";

                                int x = (getWidth() - fm.stringWidth(watermark)) / 2;
                                int y = getHeight() / 2;

                                g2.drawString(watermark, x, y);
                                g2.dispose();
                        }
                };

                watermarkPanel.setOpaque(false);
                watermarkPanel.add(ticketPanel);
                background.add(watermarkPanel);

                setContentPane(background);
                setVisible(true);
        }

        private String safe(String value) {
                return value == null ? "Not set" : value;
        }

        private JPanel makeInfoBlock(String labelText, String valueText) {
                JPanel block = new JPanel();
                block.setOpaque(false);
                block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

                JLabel label = new JLabel(labelText);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setForeground(MUTED);

                JLabel value = new JLabel(safe(valueText));
                value.setFont(new Font("Segoe UI", Font.BOLD, 15));
                value.setForeground(NAVY);

                block.add(label);
                block.add(Box.createVerticalStrut(7));
                block.add(value);

                return block;
        }

        private void addDetail(JPanel panel, String labelText,
                        String valueText) {
                JPanel block = new JPanel();
                block.setBackground(Color.WHITE);
                block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

                JLabel label = new JLabel(labelText);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setForeground(MUTED);

                JLabel value = new JLabel(safe(valueText));
                value.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                value.setForeground(TEXT);

                block.add(label);
                block.add(Box.createVerticalStrut(6));
                block.add(value);
                panel.add(block);
        }

        private void stylePrimaryButton(JButton button) {
                button.setFont(new Font("Segoe UI", Font.BOLD, 13));
                button.setForeground(Color.WHITE);
                button.setBackground(BLUE);
                button.setFocusPainted(false);
                button.setBorder(new EmptyBorder(11, 20, 11, 20));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        private void saveTicketAsPdf() {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Save BusGo E-Ticket");
                chooser.setSelectedFile(new File("BusGo_E-Ticket.pdf"));

                if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                        return;
                }

                File file = chooser.getSelectedFile();

                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                        file = new File(file.getAbsolutePath() + ".pdf");
                }

                if (file.exists()) {
                        int answer = JOptionPane.showConfirmDialog(
                                        this, "This file already exists. Replace it?",
                                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION);

                        if (answer != JOptionPane.YES_OPTION) {
                                return;
                        }
                }

                try {
                        writePdf(file);

                        JOptionPane.showMessageDialog(this,
                                        "Ticket saved successfully:\n" + file.getAbsolutePath(),
                                        "Ticket Saved", JOptionPane.INFORMATION_MESSAGE);

                } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this,
                                        "Could not save ticket: " + ex.getMessage(),
                                        "Save Error", JOptionPane.ERROR_MESSAGE);
                }
        }

        // ------------------------------------------------------------------
        // PDF output. Small self-contained writer (no external library) that
        // draws the same layout as the on-screen ticket.
        // ------------------------------------------------------------------

        // Helvetica glyph widths (1/1000 em) for ASCII 32..126.
        private static final int[] W_REGULAR = {
                        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
                        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
                        1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
                        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
                        333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
                        556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584 };

        // Helvetica-Bold glyph widths for ASCII 32..126.
        private static final int[] W_BOLD = {
                        278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
                        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
                        975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
                        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
                        333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
                        611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584 };

        private static final double PAGE_W = 595;
        private static final double PAGE_H = 842;

        private static final double[] PDF_NAVY = { 15 / 255.0, 29 / 255.0, 55 / 255.0 };
        private static final double[] PDF_BLUE = { 42 / 255.0, 104 / 255.0, 210 / 255.0 };
        private static final double[] PDF_LIGHT = { 235 / 255.0, 242 / 255.0, 252 / 255.0 };
        private static final double[] PDF_TEXT = { 45 / 255.0, 55 / 255.0, 72 / 255.0 };
        private static final double[] PDF_MUTED = { 110 / 255.0, 122 / 255.0, 142 / 255.0 };
        private static final double[] PDF_BORDER = { 220 / 255.0, 227 / 255.0, 238 / 255.0 };
        private static final double[] PDF_WHITE = { 1, 1, 1 };
        private static final double[] PDF_SUBTITLE = { 205 / 255.0, 220 / 255.0, 240 / 255.0 };
        private static final double[] PDF_GREEN = { 190 / 255.0, 245 / 255.0, 214 / 255.0 };
        private static final double[] PDF_GREEN_LINE = { 80 / 255.0, 150 / 255.0, 130 / 255.0 };

        private double textWidth(String text, boolean bold, double size) {
                int[] widths = bold ? W_BOLD : W_REGULAR;
                double total = 0;
                for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);
                        int idx = (c >= 32 && c <= 126) ? c - 32 : '?' - 32;
                        total += widths[idx];
                }
                return total * size / 1000.0;
        }

        // Splits text into lines that fit maxWidth, breaking over-long words.
        private java.util.List<String> wrap(String text, boolean bold,
                        double size, double maxWidth) {
                java.util.List<String> lines = new java.util.ArrayList<>();
                StringBuilder line = new StringBuilder();

                for (String word : text.trim().split("\\s+")) {
                        while (textWidth(word, bold, size) > maxWidth) {
                                int cut = word.length() - 1;
                                while (cut > 1 && textWidth(word.substring(0, cut), bold, size) > maxWidth) {
                                        cut--;
                                }
                                if (line.length() > 0) {
                                        lines.add(line.toString());
                                        line.setLength(0);
                                }
                                lines.add(word.substring(0, cut));
                                word = word.substring(cut);
                        }

                        String candidate = line.length() == 0 ? word : line + " " + word;
                        if (line.length() > 0 && textWidth(candidate, bold, size) > maxWidth) {
                                lines.add(line.toString());
                                line.setLength(0);
                                line.append(word);
                        } else {
                                line.setLength(0);
                                line.append(candidate);
                        }
                }

                if (line.length() > 0 || lines.isEmpty()) {
                        lines.add(line.toString());
                }
                return lines;
        }

        private static String num(double value) {
                return String.format(java.util.Locale.US, "%.2f", value);
        }

        private static String rgb(double[] c) {
                return num(c[0]) + " " + num(c[1]) + " " + num(c[2]);
        }

        private void fillRect(StringBuilder s, double x, double y,
                        double w, double h, double[] color) {
                s.append(rgb(color)).append(" rg\n")
                                .append(num(x)).append(' ').append(num(y)).append(' ')
                                .append(num(w)).append(' ').append(num(h)).append(" re f\n");
        }

        private void strokeRect(StringBuilder s, double x, double y,
                        double w, double h, double[] color) {
                s.append(rgb(color)).append(" RG\n0.8 w\n")
                                .append(num(x)).append(' ').append(num(y)).append(' ')
                                .append(num(w)).append(' ').append(num(h)).append(" re S\n");
        }

        // Draws text with its left edge at x. Returns nothing; y is the baseline.
        private void drawText(StringBuilder s, double x, double y, String text,
                        boolean bold, double size, double[] color) {
                s.append("BT\n").append(rgb(color)).append(" rg\n")
                                .append(bold ? "/F2 " : "/F1 ").append(num(size)).append(" Tf\n")
                                .append(num(x)).append(' ').append(num(y)).append(" Td\n")
                                .append('(').append(pdfEscape(text)).append(") Tj\nET\n");
        }

        private void drawTextRight(StringBuilder s, double rightX, double y,
                        String text, boolean bold, double size, double[] color) {
                drawText(s, rightX - textWidth(text, bold, size), y, text, bold, size, color);
        }

        // Draws wrapped text; returns the y of the baseline after the last line.
        private double drawWrapped(StringBuilder s, double x, double y, String text,
                        boolean bold, double size, double[] color,
                        double maxWidth, double lineHeight) {
                for (String line : wrap(text, bold, size, maxWidth)) {
                        drawText(s, x, y, line, bold, size, color);
                        y -= lineHeight;
                }
                return y;
        }

        private int lineCount(String text, boolean bold, double size, double maxWidth) {
                return wrap(text, bold, size, maxWidth).size();
        }

        private byte[] buildTicketContent() {
                final double cardX = 50;
                final double cardW = 495;
                final double pad = 30;
                final double innerW = cardW - 2 * pad;
                final double top = 790;

                StringBuilder s = new StringBuilder();
                double y = top;

                // ---- Header band ----
                double headerH = 96;
                fillRect(s, cardX, y - headerH, cardW, headerH, PDF_NAVY);
                drawText(s, cardX + pad, y - 46, "BusGo", true, 28, PDF_WHITE);
                drawText(s, cardX + pad, y - 68, "E-TICKET  |  BOOKING CONFIRMED",
                                true, 11, PDF_SUBTITLE);

                double badgeW = 92;
                double badgeH = 28;
                double badgeX = cardX + cardW - pad - badgeW;
                double badgeY = y - headerH / 2 - badgeH / 2;
                strokeRect(s, badgeX, badgeY, badgeW, badgeH, PDF_GREEN_LINE);
                drawText(s, badgeX + (badgeW - textWidth("CONFIRMED", true, 10)) / 2,
                                badgeY + 10, "CONFIRMED", true, 10, PDF_GREEN);
                y -= headerH;

                // ---- Journey band: departure | date | arrival ----
                double gap = 12;
                double colW = (innerW - 2 * gap) / 3;
                String[] jLabels = { "DEPARTURE", "JOURNEY DATE", "ARRIVAL" };
                String[] jValues = { safe(departure), safe(journeyDate), safe(arrival) };

                int jLines = 1;
                for (String v : jValues) {
                        jLines = Math.max(jLines, lineCount(v, true, 13, colW));
                }
                double bandH = 22 + 14 + jLines * 17 + 14;
                fillRect(s, cardX, y - bandH, cardW, bandH, PDF_LIGHT);
                for (int i = 0; i < 3; i++) {
                        double x = cardX + pad + i * (colW + gap);
                        drawText(s, x, y - 28, jLabels[i], true, 8.5, PDF_MUTED);
                        drawWrapped(s, x, y - 46, jValues[i], true, 13, PDF_NAVY, colW, 17);
                }
                y -= bandH;

                // ---- Details grid (2 columns) ----
                double dGap = 18;
                double dColW = (innerW - dGap) / 2;
                String[][] rows = {
                                { "PASSENGER", safe(passenger), "BUS ID", safe(busId) },
                                { "SEAT NUMBER(S)", safe(seats), "PAYMENT METHOD", safe(payment) } };

                y -= 12;
                for (String[] row : rows) {
                        int n = Math.max(lineCount(row[1], false, 12, dColW),
                                        lineCount(row[3], false, 12, dColW));
                        double rowTop = y - 12;
                        for (int col = 0; col < 2; col++) {
                                double x = cardX + pad + col * (dColW + dGap);
                                drawText(s, x, rowTop - 8, row[col * 2], true, 8.5, PDF_MUTED);
                                drawWrapped(s, x, rowTop - 26, row[col * 2 + 1], false, 12,
                                                PDF_TEXT, dColW, 16);
                        }
                        y = rowTop - 26 - (n - 1) * 16 - 12;
                }
                y -= 6;

                // ---- Fare band ----
                double fareH = 54;
                fillRect(s, cardX, y - fareH, cardW, fareH, PDF_LIGHT);
                drawText(s, cardX + pad, y - 31, "TOTAL FARE", true, 11, PDF_MUTED);
                drawTextRight(s, cardX + cardW - pad, y - 34,
                                String.format(java.util.Locale.US, "Rs. %.2f", totalAmount),
                                true, 22, PDF_BLUE);
                y -= fareH;

                // ---- Note ----
                y -= 22;
                y = drawWrapped(s, cardX + pad, y,
                                "Please keep this ticket available during your journey.",
                                false, 10, PDF_MUTED, innerW, 14);
                y -= 4;

                // ---- Card outline (spans header to bottom of note) ----
                strokeRect(s, cardX, y, cardW, top - y, PDF_BORDER);

                // Footer line below the card.
                drawText(s, cardX, y - 20, "Generated by BusGo", false, 9, PDF_MUTED);

                return s.toString().getBytes(StandardCharsets.US_ASCII);
        }

        private void writePdf(File file) throws IOException {
                byte[] streamBytes = buildTicketContent();

                ByteArrayOutputStream pdf = new ByteArrayOutputStream();
                int[] offsets = new int[7];

                writeAscii(pdf, "%PDF-1.4\n");

                offsets[1] = pdf.size();
                writeAscii(pdf, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

                offsets[2] = pdf.size();
                writeAscii(pdf, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

                offsets[3] = pdf.size();
                writeAscii(pdf, "3 0 obj\n<< /Type /Page /Parent 2 0 R "
                                + "/MediaBox [0 0 " + (int) PAGE_W + " " + (int) PAGE_H + "] "
                                + "/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> "
                                + "/Contents 6 0 R >>\nendobj\n");

                offsets[4] = pdf.size();
                writeAscii(pdf, "4 0 obj\n<< /Type /Font /Subtype /Type1 "
                                + "/BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");

                offsets[5] = pdf.size();
                writeAscii(pdf, "5 0 obj\n<< /Type /Font /Subtype /Type1 "
                                + "/BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n");

                offsets[6] = pdf.size();
                writeAscii(pdf, "6 0 obj\n<< /Length " + streamBytes.length
                                + " >>\nstream\n");
                pdf.write(streamBytes);
                writeAscii(pdf, "\nendstream\nendobj\n");

                int xrefOffset = pdf.size();
                writeAscii(pdf, "xref\n0 7\n");
                writeAscii(pdf, "0000000000 65535 f \n");

                for (int i = 1; i <= 6; i++) {
                        writeAscii(pdf, String.format(
                                        java.util.Locale.US, "%010d 00000 n \n", offsets[i]));
                }

                writeAscii(pdf, "trailer\n<< /Size 7 /Root 1 0 R >>\n"
                                + "startxref\n" + xrefOffset + "\n%%EOF");

                try (FileOutputStream output = new FileOutputStream(file)) {
                        pdf.writeTo(output);
                }
        }

        private String pdfEscape(String text) {
                return text.replace("\\", "\\\\")
                                .replace("(", "\\(")
                                .replace(")", "\\)")
                                .replaceAll("[^\\x20-\\x7E]", "?");
        }

        private void writeAscii(OutputStream output, String text)
                        throws IOException {
                output.write(text.getBytes(StandardCharsets.US_ASCII));
        }
}