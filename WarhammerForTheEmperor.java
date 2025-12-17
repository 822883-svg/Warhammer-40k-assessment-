import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

// ===================================================================
// MAIN FRAME
// ===================================================================

public class WarhammerForTheEmperor extends JFrame {

    CardLayout cardLayout = new CardLayout();
    JPanel container = new JPanel(cardLayout);

    public static void main(String[] args) {
        new WarhammerForTheEmperor();
    }

    public WarhammerForTheEmperor() {
        setTitle("Warhammer 40k: For the Emperor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // PANELS
        
        MainMenuPanel menu = new MainMenuPanel(this);
        GamePanel game = new GamePanel();
      
      
        container.add(menu, "MENU");
        container.add(game, "GAME");
        add(container);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void startGame() {
        cardLayout.show(container, "GAME");
    }

    
}
// ===================================================================
// MAIN MENU PANEL
// ===================================================================

class MainMenuPanel extends JPanel {

    public MainMenuPanel(WarhammerForTheEmperor frame) {
        setPreferredSize(new Dimension(900, 900));
        setBackground(Color.BLACK);
        setLayout(null);

        JLabel title = new JLabel("WARHAMMER 40K", SwingConstants.CENTER);
        title.setBounds(0, 220, 900, 70);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        add(title);

        JLabel subtitle = new JLabel("FOR THE EMPEROR", SwingConstants.CENTER);
        subtitle.setBounds(0, 290, 900, 40);
        subtitle.setFont(new Font("Arial", Font.BOLD, 28));
        subtitle.setForeground(Color.LIGHT_GRAY);
        add(subtitle);

        JButton playButton = new JButton("PLAY");
        playButton.setBounds(350, 420, 200, 60);
        playButton.setFont(new Font("Arial", Font.BOLD, 26));
        add(playButton);

        JLabel SpaceMarineDesignator = new JLabel("Name: ", SwingConstants.CENTER);
        SpaceMarineDesignator.setBounds(350, 550, 100, 60);
        SpaceMarineDesignator.setFont(new Font("Arial", Font.BOLD, 26));
        add(SpaceMarineDesignator);

        JTextField MarineUser  = new JTextField(15);
        MarineUser.setBounds(480, 550, 100, 60);
        add(MarineUser); 

         JLabel SpaceMarinePassword = new JLabel("Pass: ", SwingConstants.CENTER);
        SpaceMarinePassword.setBounds(350, 670, 100, 60);
        SpaceMarinePassword.setFont(new Font("Arial", Font.BOLD, 26));
        add(SpaceMarinePassword);

        JTextField MarinePassKey  = new JTextField(15);
        MarinePassKey.setBounds(480, 670, 100, 60);
        add(MarinePassKey); 

        playButton.addActionListener(e -> frame.startGame());
    }
}

// ===================================================================
// GAME PANEL 
// ===================================================================

class GamePanel extends JPanel implements ActionListener, MouseMotionListener, MouseListener {

    private final int WIDTH = 900;
    private final int HEIGHT = 900;

    Rectangle player = new Rectangle(300, 700, 30, 30);
    ArrayList<Rectangle> enemies = new ArrayList<>();
    ArrayList<Rectangle> shots = new ArrayList<>();

    ArrayList<Rectangle> goldPowerups = new ArrayList<>();
    ArrayList<Rectangle> bluePowerups = new ArrayList<>();

    Rectangle boss = null;
    int bossHP = 0;

    class Explosion {
        int x, y, size, timer;
        Explosion(int x, int y) {
            this.x = x;
            this.y = y;
            this.size = 10;
            this.timer = 10;
        }
    }
    ArrayList<Explosion> explosions = new ArrayList<>();

    boolean immortal = false;
    long immortalEndTime = 0;
    boolean shield = false;

    Random rand = new Random();
    int score = 0;
    int lives = 3;

    int[] scoreHistory = new int[10];
    int scoreIndex = 0;

    Timer timer;

    // STARFIELD
    class Star {
        int x, y, size, speed;
        boolean roundShape;

        Star(int x, int y, int size, int speed, boolean roundShape) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.roundShape = roundShape;
        }
    }

    ArrayList<Star> stars = new ArrayList<>();

    private void initStars() {
        for (int i = 0; i < 120; i++) {
            stars.add(new Star(
                    rand.nextInt(WIDTH),
                    rand.nextInt(HEIGHT),
                    rand.nextInt(4) + 2,
                    rand.nextInt(3) + 1,
                    rand.nextBoolean()
            ));
        }
    }

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);

        addMouseMotionListener(this);
        addMouseListener(this);

        initStars();
        spawnEnemy();

        timer = new Timer(16, this);
        timer.start();
    }

    private void spawnEnemy() {
        enemies.add(new Rectangle(rand.nextInt(WIDTH - 30), 0, 30, 30));
    }

    private void spawnBoss() {
        boss = new Rectangle(WIDTH / 2 - 50, 0, 100, 60);
        bossHP = 8;
    }

    private void spawnGoldPowerup() {
        goldPowerups.add(new Rectangle(rand.nextInt(WIDTH - 30), 0, 30, 30));
    }

    private void spawnBluePowerup() {
        bluePowerups.add(new Rectangle(rand.nextInt(WIDTH - 30), 0, 30, 30));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (immortal && System.currentTimeMillis() > immortalEndTime)
            immortal = false;

        for (Star s : stars) {
            s.y += s.speed;
            if (s.y > HEIGHT) {
                s.y = 0;
                s.x = rand.nextInt(WIDTH);
            }
        }

        Iterator<Rectangle> ei = enemies.iterator();
        while (ei.hasNext()) {
            Rectangle enemy = ei.next();
            enemy.y += 3;

            if (enemy.intersects(player)) {
                ei.remove();
                if (!immortal) {
                    if (shield) shield = false;
                    else {
                        lives--;
                        if (lives <= 0) gameOver();
                    }
                }
            }

            if (enemy.y > HEIGHT) ei.remove();
        }

        if (boss != null) {
            boss.y += 2;
            if (boss.intersects(player)) {
                if (!immortal) {
                    if (shield) shield = false;
                    else {
                        lives--;
                        if (lives <= 0) gameOver();
                    }
                }
                boss = null;
            }
            if (boss != null && boss.y > HEIGHT) boss = null;
        }

        Iterator<Rectangle> si = shots.iterator();
        while (si.hasNext()) {
            Rectangle shot = si.next();
            shot.y -= 10;

            if (shot.y < -20) {
                si.remove();
                continue;
            }

            Iterator<Rectangle> ei2 = enemies.iterator();
            while (ei2.hasNext()) {
                Rectangle enemy = ei2.next();
                if (shot.intersects(enemy)) {
                    si.remove();
                    ei2.remove();
                    score++;
                    explosions.add(new Explosion(enemy.x + 15, enemy.y + 15));
                    break;
                }
            }

            if (boss != null && shot.intersects(boss)) {
                si.remove();
                bossHP--;
                if (bossHP <= 0) {
                    explosions.add(new Explosion(boss.x + 50, boss.y + 30));
                    boss = null;
                    score += 5;
                }
            }
        }

        Iterator<Explosion> exi = explosions.iterator();
        while (exi.hasNext()) {
            Explosion ex = exi.next();
            ex.size += 3;
            ex.timer--;
            if (ex.timer <= 0) exi.remove();
        }

        Iterator<Rectangle> gi = goldPowerups.iterator();
        while (gi.hasNext()) {
            Rectangle gp = gi.next();
            gp.y += 3;
            if (gp.intersects(player)) {
                immortal = true;
                immortalEndTime = System.currentTimeMillis() + 3000;
                gi.remove();
            }
            if (gp.y > HEIGHT) gi.remove();
        }

        Iterator<Rectangle> bi = bluePowerups.iterator();
        while (bi.hasNext()) {
            Rectangle bp = bi.next();
            bp.y += 3;
            if (bp.intersects(player)) {
                shield = true;
                bi.remove();
            }
            if (bp.y > HEIGHT) bi.remove();
        }

        if (rand.nextInt(20) == 0) spawnEnemy();
        if (rand.nextInt(800) == 0) spawnGoldPowerup();
        if (rand.nextInt(900) == 0) spawnBluePowerup();
        if (boss == null && rand.nextInt(1000) == 0) spawnBoss();

        repaint();
    }

    private void gameOver() {
        timer.stop();
        JOptionPane.showMessageDialog(this, "GAME OVER!\nScore: " + score);
        System.exit(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.WHITE);
        for (Star s : stars) {
            if (s.roundShape) g2.fillOval(s.x, s.y, s.size, s.size);
            else g2.fillRect(s.x, s.y, s.size, s.size);
        }

        if (immortal) {
            g2.setColor(new Color(255, 215, 0, 120));
            g2.fillOval(player.x - 10, player.y - 10,
                    player.width + 20, player.height + 20);
        }

        g2.setColor(Color.CYAN);
        g2.fillRect(player.x, player.y, player.width, player.height);

        g2.setColor(Color.MAGENTA);
        for (Rectangle r : enemies)
            g2.fillRect(r.x, r.y, r.width, r.height);

        if (boss != null) {
            g2.setColor(Color.RED);
            g2.fillRect(boss.x, boss.y, boss.width, boss.height);
            g2.setColor(Color.WHITE);
            g2.drawString("HP: " + bossHP, boss.x + 20, boss.y + 30);
        }

        g2.setColor(Color.WHITE);
        for (Rectangle s : shots)
            g2.fillRect(s.x, s.y, s.width, s.height);

        for (Explosion ex : explosions) {
            g2.setColor(Color.ORANGE);
            g2.fillOval(ex.x - ex.size / 2, ex.y - ex.size / 2, ex.size, ex.size);
        }

        g2.setColor(Color.YELLOW);
        for (Rectangle gp : goldPowerups)
            g2.fillRect(gp.x, gp.y, gp.width, gp.height);

        g2.setColor(Color.BLUE);
        for (Rectangle bp : bluePowerups)
            g2.fillRect(bp.x, bp.y, bp.width, bp.height);

        g2.setColor(Color.WHITE);
        g2.drawString("Score: " + score, 10, 20);
        for (int i = 0; i < lives; i++) {
    g2.setColor(Color.RED);
    g2.fillOval(10 + i * 25, 50, 18, 18);
    g2.setColor(Color.DARK_GRAY);
    g2.drawOval(10 + i * 25, 50, 18, 18);
}
    }

    @Override public void mouseMoved(MouseEvent e) {
        player.x = e.getX() - player.width / 2;
        player.y = e.getY() - player.height / 2;
    }
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) {
        int cx = player.x + player.width / 2;
        shots.add(new Rectangle(cx - 12, player.y - 10, 6, 12));
        shots.add(new Rectangle(cx + 6, player.y - 10, 6, 12));
    }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
