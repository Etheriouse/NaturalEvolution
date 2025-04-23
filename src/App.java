import java.awt.Color;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Vector;

public class App {

    public static void main(String[] args) throws Exception {
        Window w = new Window("Evolution System");
        Perlin1D p = new Perlin1D(1000, System.currentTimeMillis());

        int left = KeyEvent.VK_Q, right = KeyEvent.VK_D, up = KeyEvent.VK_Z, down = KeyEvent.VK_S,
                space = KeyEvent.VK_SPACE, escape = KeyEvent.VK_ESCAPE;

        boolean start = false;

        double dt = 0.1;
        double tick = 0;

        Particule p1 = new Particule(new vect(1000, 200), new vect(0, 0), 5, 10, 10, 10);
        Particule p2 = new Particule(new vect(1100, 200), new vect(0, 0), 5, 10, 10, 10);
        Particule p3 = new Particule(new vect(1000, 100), new vect(0, 0), 4, 10, 10, 10);

        Entity e = new Entity(new Particule[]{p1, p2, p3}, new Stick[]{
                new Stick(p1, p2), new Stick(p2, p3)});
        Entity er = new Entity(10, 10, 10, 700);

        while (!w.keypressed(escape)) {
            w.clear(Color.gray);

            for (int i = 0; i < 5000; i += 10) {
                w.drawLine(i, (int) (Particule.floor(i, p)), i + 10, (int) (Particule.floor(i + 10, p)), 4,
                        Color.black);
            }

            // e.draw(w);
            er.draw(w);
            e.draw(w);

            if (w.keypressed(space) && w.cooldown(space, 200)) {
                w.resetcooldown(space);
                System.out.println("wtf");
                start = !start;
            }

            // if(w.keypressed(left) && w.cooldown(left, 200)) {
            // w.resetcooldown(left);
            // p1.addStrenght(new vect(-10, 0));
            // }

            // if(w.keypressed(right) && w.cooldown(right, 200)) {
            // w.resetcooldown(right);
            // p1.addStrenght(new vect(10, 0));
            // }

            if(w.keypressed(up) && w.cooldown(up, 200)) {
                w.resetcooldown(up);
                p1.addStrenght(new vect(0, -10));
            }

            // if(w.keypressed(down) && w.cooldown(down, 200)) {
            // w.resetcooldown(down);
            // p1.addStrenght(new vect(0, 10));
            // }

            if (start) {
                er.process(dt, tick, p);
                e.process(dt, tick, p);

                // e.process(dt);
            }

            tick += dt;

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
        x *= dt;
        y *= dt;
        return this;
    }

    public double norm() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public vect normalize() {
        double len = norm();
        return len > 0 ? this.scale(1.0 / len) : new vect(0, 0);
    }

    public vect other() {
        return new vect(x, y);
    }

    public void reset() {
        x = 0;
        y = 0;
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y;
    }
}

class Particule {

    public vect position;
    public vect prev_pos;
    public vect velocity;
    public double mass;

    class Movement {
        public vect strength;
        public double tick_len;

        public Movement(vect strength, double tick_len) {
            this.strength = strength;
            this.tick_len = tick_len;
        }
    }

    public ArrayList<Movement> Movements = new ArrayList<>();
    public int actual_mov = 0;
    public double start_dt_actual = 0;

    public Particule(vect pos, vect vel, double mass, double strenght, double endurance, int inteligence) {
        position = pos;
        prev_pos = pos.other();
        velocity = vel;
        this.mass = mass;

        for (int i = 0; i < inteligence; i++) {
            double tx = Math.random() * strenght;
            double ty = Math.random() * strenght;
            Movements.add(new Movement(
                    new vect(tx * (Math.random() * 10 % 2 == 0 ? -1 : 1), ty * (Math.random() * 10 % 2 == 0 ? -1 : 1)),
                    Math.random() * endurance + 1));
        }
    }

    public void mov(double tick) {
        if (actual_mov < Movements.size()) {
            if (start_dt_actual + Movements.get(actual_mov).tick_len > tick) {
                velocity.add(Movements.get(actual_mov).strength);
            } else {
                start_dt_actual = tick;
                actual_mov++;
            }
        } else {
            actual_mov = 0;
            start_dt_actual = 0;
        }
    }

    public void addStrenght(vect strenght) {
        velocity.add(strenght);
    }

    public void process(double dt, Perlin1D p) {
        if (position.y + ((mass * 10) / 2) > floor(position.x, p)) {
            position.y = floor(position.x, p) - ((mass * 10) / 2);
            prev_pos.y = floor(position.x, p) - ((mass * 10) / 2);
            velocity.add(velocity.other().scale(-1)); // friction du sol

        }

        vect new_prev_pos = position.other();
        velocity.add(velocity.other().scale(-0.1)); // friction de l'air
        vect acc = velocity.other().scale(1.0 / mass);
        position.add(position.other().sub(prev_pos)).add(acc.scale(dt * dt));
        prev_pos = new_prev_pos;
        velocity.reset();
    }

    public void draw(Window w) {
        w.drawCircleFill((int) (position.x - (((mass * 10 / 2)))), (int) (position.y - ((mass * 10) / 2)),
                (int) mass * 10, Color.cyan);
    }

    @Override
    public String toString() {
        return "Position: " + position.toString() + " Velocity: " + velocity.toString() + " Mass: " + mass;
    }

    public static double floor(double x, Perlin1D p) {
        return (p.get(x * 0.01) * 100.0) + 1000;
        // sin(x)+0.5 sin(3 x+1.5)+0.25 sin(5 x)
    }

}

class Stick {

    public Particule p1, p2;
    public double size;

    public Stick(Particule p1, Particule p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.size = p1.position.other().sub(p2.position).norm();
    }

    public void fix_() {
        vect delta = p2.position.other().sub(p1.position);
        if (delta.norm() == size) {
            return;
        }
        double d = delta.norm();
        double error = d - size;
        vect correct = delta.scale(error / d).scale(0.5);
        p1.position.add(correct);
        p2.position.sub(correct);
    }

    public void draw(Window w) {
        w.drawLine((int) p1.position.x, (int) p1.position.y, (int) p2.position.x, (int) p2.position.y, 5, Color.white);
    }

    @Override
    public String toString() {
        return "Size: " + size;
    }
}

class Entity {

    public ArrayList<Stick> Sticks = new ArrayList<>();
    public ArrayList<Particule> Particules = new ArrayList<>();

    public Entity(int nb_max_particules, int nb_max_sticks, int max_mass, int max_size_stick) {
        int nb_particules = (int) (Math.random() * nb_max_particules) + 1;
        int nb_sticks = (int) (Math.random() * nb_max_sticks) + 1;

        double endurance = Math.random() * 0;
        double strength = Math.random() * 1 + 1;
        int inteligence = (int) Math.random() * 5 + 1;

        for (int i = 0; i < nb_particules; i++) {
            Particule p = new Particule(
                    new vect((int) (Math.random() * max_size_stick), (int) (Math.random() * max_size_stick)),
                    new vect(0, 0), Math.random() * max_mass + 1, strength, endurance, inteligence);
            Particules.add(p);
        }

        for (int i = 0; i < nb_sticks; i++) {
            int p1 = (int) (Math.random() * nb_particules);
            int p2 = (int) (Math.random() * nb_particules);
            while (p1 == p2) {
                p2 = (int) (Math.random() * nb_particules);
            }
            Stick s = new Stick(Particules.get(p1), Particules.get(p2));
            Sticks.add(s);
        }

        Particules.removeIf(p -> Sticks.stream().noneMatch(s -> s.p1 == p || s.p2 == p));
        ArrayList<Particule> connected = new ArrayList<>();
        ArrayList<Stick> connectedSticks = new ArrayList<>();
        connected.add(Particules.get(0));

        for (int i = 0; i < connected.size(); i++) {
            Particule current = connected.get(i);
            for (Stick s : Sticks) {
                if (s.p1 == current && !connected.contains(s.p2)) {
                    connected.add(s.p2);
                    connectedSticks.add(s);
                } else if (s.p2 == current && !connected.contains(s.p1)) {
                    connected.add(s.p1);
                    connectedSticks.add(s);
                }
            }
        }

        Particules = connected;
        Sticks = connectedSticks;
    }

    public Entity(Particule p[], Stick s[]) {
        for (Particule p1 : p) {
            Particules.add(p1);
        }
        for (Stick s1 : s) {
            Sticks.add(s1);
        }
    }

    public void draw(Window w) {
        for (Stick s : Sticks) {
            s.draw(w);
        }
        for (Particule p : Particules) {
            p.draw(w);
        }
    }

    public void process(double dt, double tick, Perlin1D pd) {
        for (Particule p : Particules) {
            p.mov(tick);
            p.process(dt, pd);
        }
        gravity();
        for (Stick s : Sticks) {
            s.fix_();
        }
    }

    public void mov(vect pos) {
        for (Particule p : Particules) {
            p.position.add(pos);
        }
    }

    public void gravity() {
        for (Particule p : Particules) {
            p.addStrenght(new vect(0, 9.81 * p.mass));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Particules: \n");
        for (Particule p : Particules) {
            sb.append(p.toString()).append("\n");
        }
        sb.append("Sticks: \n");
        for (Stick s : Sticks) {
            sb.append(s.toString()).append("\n");
        }
        return sb.toString();
    }

}
