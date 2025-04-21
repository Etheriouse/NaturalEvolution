import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GradientPaint;

import javax.swing.JComponent;
import javax.swing.JFrame;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;

public class Window extends JFrame {

    public int width = 960;
    public int height = 540;

    public int x_offset = (int) Math.round((width * 0.052083) / 10.0f) * 10;

    public int xMouse = 0;
    public int yMouse = 0;

    public boolean Click = false;

    public boolean fullscreen = false;

    public boolean fenetrer_without_border = true;

    public int divident_ts = 18;

    public int Ts = 60;
    
    private BufferedImage onscreenImage = new BufferedImage(5000, 5000, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage offscreenImage = new BufferedImage(5000, 5000, BufferedImage.TYPE_INT_ARGB);

    private Graphics2D offscreen = offscreenImage.createGraphics();
    private Graphics2D onscreen = onscreenImage.createGraphics();

    private AffineTransform rotation = new AffineTransform();

    private ImagePanel panel = new ImagePanel(onscreenImage);

    public TreeSet<Integer> keysDown;
    private TreeMap<Integer, Long> cooldown = new TreeMap<>();

    private Image cursor_clicked = null;
    private Cursor _cursor_clicked = null;
    private Image cursor = null;
    private Cursor _cursor = null;

    public int getwidth() {
        return width;
    }

    public int getheight() {
        return height;
    }

    public void setheight(int n) {
        height = n + this.getInsets().top;
        this.setSize(width, height);
    }

    public void setwidth(int n) {
        width = n;
        this.setSize(width, height);
    }

    public void _setSize(int w, int h) {
        this.setheight(h);
        this.setwidth(w);
    }

    public void _setCursor(Image cursor) {
        this.cursor = cursor;
        this._cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursor, new Point(0, 0), "gauntlet cursor");
        this.setCursor(_cursor);
    }

    public void _setFontFromTtf(File f) {
        try {
            setStringFont(Font.createFont(0, f));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public void _setCursorClicked(Image cursor_clicked) {
        this.cursor_clicked = cursor_clicked;
        this._cursor_clicked = Toolkit.getDefaultToolkit().createCustomCursor(cursor_clicked, new Point(0, 0), "gauntlet cursor clicked");
    }

    public Window(String name) {
        Setup(name);
    }

    private void Setup(String name_windows) {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle(name_windows);
        
        if (fullscreen) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

            this.setUndecorated(true);
            gd.setFullScreenWindow(this); // Mettre le JFrame en plein écran

            height = Toolkit.getDefaultToolkit().getScreenSize().height;
            width = Toolkit.getDefaultToolkit().getScreenSize().width;
            Ts = (int) (height / divident_ts);
        
        } else if (fenetrer_without_border) {
        
            this.setUndecorated(true); // Supprimer la barre de titre et les bordures
        
            height = Toolkit.getDefaultToolkit().getScreenSize().height;
            width = Toolkit.getDefaultToolkit().getScreenSize().width;
        
            Ts = (int) (height / divident_ts);
        
            this.setSize(width, height); 
        } else {
            this.setSize(width, height);
        }
        this.setContentPane(panel);
        this.setVisible(true);

        keysDown = new TreeSet<Integer>();
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //System.out.println(e.getKeyCode());
                keysDown.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keysDown.remove(e.getKeyCode());
            }
        });
        int MarginTop = this.getInsets().top;
        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xMouse = e.getX();
                yMouse = e.getY() - MarginTop;
            }

        });
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                xMouse = e.getX();
                yMouse = e.getY() - MarginTop;

                changeCursor(true);
                Click = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                changeCursor(false);
                Click = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                xMouse = e.getX();
                yMouse = e.getY() - MarginTop;
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

        });


        for(int i = 0; i<128; i++) {
            resetcooldown(i);
        }
        
    }

    public boolean keypressed(int key) {
        return this.keysDown.contains(key);
    }

    public long getcooldown(int key) {
        return cooldown.get(key);
    }

    public boolean cooldown(int key, int cooldown_) {
        return System.currentTimeMillis()-cooldown.get(key) > cooldown_;
    }

    public void resetcooldown(int key) {
        cooldown.put(key, System.currentTimeMillis());
    }

    private void changeCursor(boolean clicked) {
        if(_cursor != null && _cursor_clicked != null) {
            if (clicked) {
                this.setCursor(_cursor_clicked);
            } else {
                this.setCursor(_cursor);
            }
        }
    }

    public void refresh() {
        onscreen.drawImage(offscreenImage, 0, 0, null);
        panel.repaint();
        width = panel.getWidth();
        height = panel.getHeight();
    }

    public void clear() {
        offscreen.setColor(Color.BLACK);
        offscreen.fillRect(0, 0, width, height);
    }

    public void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
    }

    public void setStringFont(Font f) {
        offscreen.setFont(f);
    }

    public void drawCircle(int x, int y, int radius, Color color) {
        offscreen.setColor(color);
        offscreen.drawOval(x, y, radius, radius);
    }

    public void drawCircle(int x, int y, int radius, String color) {
        offscreen.setColor(Color.decode(color));
        offscreen.drawOval(x, y, radius, radius);
    }

    public void drawCircleFill(int x, int y, int radius, Color color) {
        offscreen.setColor(color);
        offscreen.fillOval(x, y, radius, radius);
    }

    public void drawCircleFill(int x, int y, int radius, String color) {
        offscreen.setColor(Color.decode(color));
        offscreen.fillOval(x, y, radius, radius);
    }

    public void drawLine(int x, int y, int x_, int y_, int height, Color color) {
        offscreen.setStroke(new BasicStroke(height));
        offscreen.setColor(color);
        offscreen.drawLine(x, y, x_, y_);
    }

    public void drawString(String s, int size, int x, int y) {
        offscreen.setFont(offscreen.getFont().deriveFont(0, size));
        offscreen.drawString(s, x, y);
    }

    public void drawString(String s, int size, int x, int y, String c) {
        offscreen.setColor(Color.decode(c));
        offscreen.setFont(offscreen.getFont().deriveFont(0, size));
        offscreen.drawString(s, x, y);
    }

    public void drawString(String s, int size, int x, int y, String c, String over) {
        offscreen.setFont(offscreen.getFont().deriveFont(0, size));

        int over_size = size/16;

        offscreen.setColor(Color.decode(over));
        offscreen.drawString(s, x-over_size, y+over_size);
        offscreen.setColor(Color.decode(over));
        offscreen.drawString(s, x-over_size, y-over_size);
        offscreen.setColor(Color.decode(over));
        offscreen.drawString(s, x+over_size, y+over_size);
        offscreen.setColor(Color.decode(over));
        offscreen.drawString(s, x+over_size, y-over_size);


        offscreen.setColor(Color.decode(c));
        offscreen.drawString(s, x, y);
    }

    public void drawTexture(int x, int y, int sizeX, int sizeY, Image texture) {
        offscreen.drawImage(texture, x, y, sizeX, sizeY, null);
    }

    public void drawTexture(int x, int y, int sizeX, int sizeY, int angle, Image texture) {
        rotation.rotate(Math.toRadians(angle), x + (sizeX / 2), y + (sizeY / 2));
        offscreen.setTransform(rotation);
        offscreen.drawImage(texture, x, y, sizeX, sizeY, null);
        rotation.rotate(Math.toRadians(-angle), x + (sizeX / 2), y + (sizeY / 2));
        offscreen.setTransform(rotation);
    }

    public void drawGradient(int x, int y, int width, int height, String color1, String color2) {
        Color couleur1 = Color.decode(color1);
        Color couleur2 = Color.decode(color2);

        GradientPaint gradient = new GradientPaint(x, y + (height / 2), couleur1, x + width, y + (height / 2),
                couleur2);
        offscreen.setPaint(gradient);
        offscreen.fillRect(x, y, width, height);
    }

    public void setColor(Color c) {
        offscreen.setColor(c);
    }

    public void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void exit() {
        this.dispose();
    }
}

class ImagePanel extends JComponent {
    private Image image;

    public ImagePanel(Image image) {
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }

    public void setImage(Image i) {
        this.image = i;
    }
}