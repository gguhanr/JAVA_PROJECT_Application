import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class GradeCalculator extends JFrame {

    private static final int    NUM_SUBJECTS          = 5;
    private static final double MAX_MARKS_PER_SUBJECT = 100.0;

    private static final Color BG_PAGE    = new Color(244, 246, 250);
    private static final Color BG_CARD    = new Color(255, 255, 255);
    private static final Color BG_SURFACE = new Color(247, 249, 252);
    private static final Color BORDER     = new Color(220, 225, 235);
    private static final Color TEXT_MAIN  = new Color(28,  36,  56 );
    private static final Color TEXT_MUTED = new Color(110, 120, 140);
    private static final Color ACCENT     = new Color(52,  120, 220);

    private record GradeConfig(int min, String grade, String desc, Color bar, Color bg, Color border) {}

    private static final GradeConfig[] GRADES = {
        new GradeConfig(90, "A+", "Outstanding",           new Color(99,  153, 34),  new Color(234, 243, 222), new Color(151, 196, 89)),
        new GradeConfig(80, "A",  "Excellent",              new Color(29,  158, 117), new Color(225, 245, 238), new Color(93,  202, 165)),
        new GradeConfig(70, "B",  "Good",                   new Color(55,  138, 221), new Color(230, 241, 251), new Color(133, 183, 235)),
        new GradeConfig(60, "C",  "Average",                new Color(186, 117, 23),  new Color(250, 238, 218), new Color(239, 159, 39)),
        new GradeConfig(50, "D",  "Below average",          new Color(216, 90,  48),  new Color(250, 236, 231), new Color(240, 153, 123)),
        new GradeConfig(0,  "F",  "Fail — needs more work", new Color(226, 75,  74),  new Color(252, 235, 235), new Color(240, 149, 149)),
    };

    private JTextField[] fields;
    private JLabel[]     fieldErrors;

    private JPanel   resultsPanel;
    private JLabel   metricTotal, metricAvg, metricPct;
    private JLabel   gradeTag, gradeDesc;
    private JPanel   gradeBadge;
    private BarPanel progressBar;

    GradeCalculator() {
        setTitle("Grade Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_PAGE);

        JPanel card = new RoundedPanel(16, BG_CARD, BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(buildHeader());
        card.add(buildDivider());
        card.add(buildFields());
        card.add(buildActions());

        resultsPanel = buildResults();
        resultsPanel.setVisible(false);
        card.add(resultsPanel);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_PAGE);
        outer.setBorder(new EmptyBorder(28, 28, 28, 28));
        outer.add(card);

        add(outer);
        pack();
        setMinimumSize(new Dimension(420, 0));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(new EmptyBorder(20, 22, 16, 22));

        JLabel title = new JLabel("Grade calculator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Enter marks out of 100 for each subject");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        return p;
    }

    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JPanel buildFields() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_CARD);
        p.setBorder(new EmptyBorder(16, 22, 8, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fields      = new JTextField[NUM_SUBJECTS];
        fieldErrors = new JLabel[NUM_SUBJECTS];
        String[] names = {"Subject 1", "Subject 2", "Subject 3", "Subject 4", "Subject 5"};

        for (int i = 0; i < NUM_SUBJECTS; i++) {
            gbc.gridx = 0; gbc.gridy = i * 2; gbc.weightx = 0.32;
            gbc.insets = new Insets(i == 0 ? 0 : 10, 0, 2, 12);
            JLabel lbl = new JLabel(names[i]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(TEXT_MUTED);
            p.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 0.68;
            gbc.insets = new Insets(i == 0 ? 0 : 10, 0, 2, 0);
            fields[i] = new JTextField();
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            fields[i].setForeground(TEXT_MAIN);
            fields[i].setBackground(BG_SURFACE);
            fields[i].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(7, 10, 7, 10)));
            fields[i].setPreferredSize(new Dimension(160, 36));
            final int idx = i;
            fields[i].addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { clearFieldError(idx); }
            });
            p.add(fields[i], gbc);

            gbc.gridx = 1; gbc.gridy = i * 2 + 1;
            gbc.insets = new Insets(0, 0, 0, 0);
            fieldErrors[i] = new JLabel(" ");
            fieldErrors[i].setFont(new Font("Segoe UI", Font.PLAIN, 11));
            fieldErrors[i].setForeground(new Color(200, 50, 50));
            p.add(fieldErrors[i], gbc);
        }
        return p;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
        p.setBackground(BG_CARD);
        p.setBorder(new EmptyBorder(12, 22, 20, 22));

        JButton calc  = styledButton("Calculate", ACCENT, Color.WHITE);
        JButton reset = styledButton("Reset", BG_SURFACE, TEXT_MAIN);
        calc.addActionListener(e  -> calculate());
        reset.addActionListener(e -> reset());

        p.add(calc);
        p.add(reset);
        return p;
    }

    private JPanel buildResults() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);

        p.add(buildDivider());

        JPanel metrics = new JPanel(new GridLayout(1, 3, 10, 0));
        metrics.setBackground(BG_CARD);
        metrics.setBorder(new EmptyBorder(16, 22, 0, 22));
        metricTotal = new JLabel("—");
        metricAvg   = new JLabel("—");
        metricPct   = new JLabel("—");
        metrics.add(metricTile("Total",      metricTotal));
        metrics.add(metricTile("Average",    metricAvg));
        metrics.add(metricTile("Percentage", metricPct));
        p.add(metrics);

        JPanel barWrap = new JPanel(new BorderLayout());
        barWrap.setBackground(BG_CARD);
        barWrap.setBorder(new EmptyBorder(14, 22, 2, 22));
        progressBar = new BarPanel();
        barWrap.add(progressBar, BorderLayout.CENTER);
        p.add(barWrap);

        JPanel barLabels = new JPanel(new BorderLayout());
        barLabels.setBackground(BG_CARD);
        barLabels.setBorder(new EmptyBorder(2, 22, 0, 22));
        JLabel l0 = tinyLabel("0%"), l50 = tinyLabel("50%"), l100 = tinyLabel("100%");
        l50.setHorizontalAlignment(SwingConstants.CENTER);
        barLabels.add(l0,   BorderLayout.WEST);
        barLabels.add(l50,  BorderLayout.CENTER);
        barLabels.add(l100, BorderLayout.EAST);
        p.add(barLabels);

        gradeBadge = new RoundedPanel(10, BG_SURFACE, BORDER);
        gradeBadge.setLayout(new BorderLayout(12, 0));
        gradeBadge.setBorder(new EmptyBorder(14, 16, 14, 16));

        gradeTag = new JLabel("—");
        gradeTag.setFont(new Font("Segoe UI", Font.BOLD, 30));
        gradeTag.setForeground(TEXT_MAIN);

        gradeDesc = new JLabel("—");
        gradeDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gradeDesc.setForeground(TEXT_MUTED);
        gradeDesc.setHorizontalAlignment(SwingConstants.RIGHT);

        gradeBadge.add(gradeTag,  BorderLayout.WEST);
        gradeBadge.add(gradeDesc, BorderLayout.EAST);

        JPanel badgeWrap = new JPanel(new BorderLayout());
        badgeWrap.setBackground(BG_CARD);
        badgeWrap.setBorder(new EmptyBorder(10, 22, 20, 22));
        badgeWrap.add(gradeBadge);
        p.add(badgeWrap);

        return p;
    }

    private JPanel metricTile(String label, JLabel valueLabel) {
        JPanel tile = new RoundedPanel(8, BG_SURFACE, new Color(0, 0, 0, 0));
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(TEXT_MAIN);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        tile.add(lbl);
        tile.add(Box.createVerticalStrut(4));
        tile.add(valueLabel);
        return tile;
    }

    private JLabel tinyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(9, 0, 9, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color hoverBg = bg.equals(BG_SURFACE) ? BORDER : bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private void calculate() {
        clearAllErrors();
        double[] marks = new double[NUM_SUBJECTS];
        boolean hasError = false;

        for (int i = 0; i < NUM_SUBJECTS; i++) {
            String txt = fields[i].getText().trim();
            try {
                double v = Double.parseDouble(txt);
                if (v < 0 || v > MAX_MARKS_PER_SUBJECT) throw new NumberFormatException();
                marks[i] = v;
            } catch (NumberFormatException ex) {
                setFieldError(i, txt.isEmpty() ? "Required" : "Must be 0 – 100");
                hasError = true;
            }
        }

        if (hasError) return;

        double total = 0;
        for (double m : marks) total += m;
        double avg = total / NUM_SUBJECTS;
        double pct = (total / (NUM_SUBJECTS * MAX_MARKS_PER_SUBJECT)) * 100;

        GradeConfig cfg = GRADES[GRADES.length - 1];
        for (GradeConfig g : GRADES) { if (pct >= g.min()) { cfg = g; break; } }

        metricTotal.setText(String.format("%.1f", total));
        metricAvg.setText(String.format("%.1f", avg));
        metricPct.setText(String.format("%.1f%%", pct));

        progressBar.setProgress(pct / 100.0, cfg.bar());

        gradeTag.setText(cfg.grade());
        gradeTag.setForeground(cfg.bar());
        gradeDesc.setText(cfg.desc());

        ((RoundedPanel) gradeBadge).bg          = cfg.bg();
        ((RoundedPanel) gradeBadge).borderColor = cfg.border();
        gradeBadge.repaint();

        resultsPanel.setVisible(true);
        pack();
    }

    private void reset() {
        clearAllErrors();
        for (JTextField f : fields) f.setText("");
        resultsPanel.setVisible(false);
        progressBar.setProgress(0, ACCENT);
        pack();
    }

    private void setFieldError(int i, String msg) {
        fields[i].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 60, 60), 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        fieldErrors[i].setText(msg);
    }

    private void clearFieldError(int i) {
        fields[i].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        fieldErrors[i].setText(" ");
    }

    private void clearAllErrors() {
        for (int i = 0; i < NUM_SUBJECTS; i++) clearFieldError(i);
    }

    static class RoundedPanel extends JPanel {
        int arc;
        Color bg, borderColor;
        RoundedPanel(int arc, Color bg, Color border) {
            this.arc = arc; this.bg = bg; this.borderColor = border;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, arc, arc));
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class BarPanel extends JPanel {
        private double progress = 0;
        private Color  barColor = new Color(52, 120, 220);
        BarPanel() { setPreferredSize(new Dimension(0, 8)); setOpaque(false); }
        void setProgress(double p, Color c) {
            progress = Math.max(0, Math.min(1, p)); barColor = c; repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight(), w = getWidth();
            g2.setColor(new Color(220, 226, 238));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
            int fill = (int)(w * progress);
            if (fill > 0) {
                g2.setColor(barColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, fill, h, h, h));
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GradeCalculator::new);
    }
}
