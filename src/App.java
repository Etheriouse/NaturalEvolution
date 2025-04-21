import java.awt.Color;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Vector;

public class App {

    public static void main(String[] args) throws Exception {
        Window w = new Window("Evolution System");

        Particule p1 = new Particule(new vect(100, 450), new vect(0, 0), 2, 25);
        Particule p2 = new Particule(new vect(100, 550), new vect(0, 0), 1, 25);

        Stick s = new Stick(p1, p2, 100);

        int left = KeyEvent.VK_Q, right = KeyEvent.VK_D, up = KeyEvent.VK_Z, down = KeyEvent.VK_S, space = KeyEvent.VK_SPACE, escape = KeyEvent.VK_ESCAPE;


        boolean start = false;

        double dt = 0.16;


        while (!w.keypressed(escape)) {
            w.clear(Color.gray);
        
            s.draw(w);
            p1.draw(w);
            p2.draw(w);

            if(w.keypressed(space) && w.cooldown(space, 200)) {
                w.resetcooldown(space);
                System.out.println("wtf");
                start = !start;
            }

            if(w.keypressed(left) && w.cooldown(left, 200)) {
                w.resetcooldown(left);
                p1.addStrenght(new vect(-10, 0), dt);
            }
            
            if(w.keypressed(right) && w.cooldown(right, 200)) {
                w.resetcooldown(right);
                p1.addStrenght(new vect(10, 0), dt);
            }

            if(w.keypressed(up) && w.cooldown(up, 200)) {
                w.resetcooldown(up);
                p1.addStrenght(new vect(0, -10), dt);
            }

            if(w.keypressed(down) && w.cooldown(down, 200)) {
                w.resetcooldown(down);
                p1.addStrenght(new vect(0, 10), dt);
            }

            if (start) {
                System.out.println("p1");
                p1.process(dt);
                System.out.println("p2");
                p2.process(dt);
                System.out.println("fix");
                s.fix_();
            }

            w.refresh();
        }
        w.exit();        
    }
}

class vect {

    public double x;
    public double y;

    public vect(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public vect sub(vect t) {
        x -= t.x;
        y -= t.y;
        return this;
    }

    public vect add(vect t) {
        x += t.x;
        y += t.y;
        return this;
    }

    public vect scale(double dt) {
        x*=dt;
        y*=dt;
        return this;
    }

    public double norm() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public vect other() {
        return new vect(x, y);
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y;
    }
}

class Particule {

    public vect position;
    public vect velocity;
    public double mass;
    public double radius;

    public Particule(vect pos, vect vel, double mass, double radius) {
        position = pos;
        velocity = vel;
        this.mass = mass;
        this.radius = radius;
    }

    public void addStrenght(vect strenght, double dt) {
        vect acc = strenght.scale(1.0/mass);
        velocity.add(acc.scale(dt));
    }

    public void process(double dt) {
        System.out.println("avant: " + position);
        position.add(velocity.other().scale(dt));
        System.out.println("apres: " + position);
    }

    public void draw(Window w) {
        w.drawCircleFill((int) (position.x-(radius/2)), (int) (position.y-(radius/2)), (int)radius, Color.cyan);
    }

}

class Stick {

    public Particule p1, p2;
    public double size;

    public Stick(Particule p1, Particule p2, double size) {
        this.p1 = p1;
        this.p2 = p2;
        this.size = size;
    }

    public void fix_() {
        vect delta = p2.position.other().sub(p1.position);
        if(delta.norm() == size) {
            return;
        }
        double dist = delta.norm();
        double diff = (dist - size) / dist;

        double w1 = 1.0/p1.mass, w2 = 1.0/p2.mass, total = w1 + w2;
        vect correct = delta.other().scale(diff);

        p1.position.add(correct.other().scale(w1/total));
        p2.position.add(correct.other().scale(w2/total));
    }

    public void draw(Window w) {
        w.drawLine((int)p1.position.x, (int) p1.position.y, (int) p2.position.x, (int) p2.position.y, 5, Color.white);
    }
}

class Entity {

    public ArrayList<Stick> sticks = new ArrayList<>();

}
