import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class ChromeDinosaur extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 750;
    int boardHeight = 250;

    //images 
    Image dinosaurImg;
    Image dinosaurDeadImg;
    Image dinosaurJumpImg;
    Image dinosaurDuckImg;
    Image cactus1Img;
    Image cactus2Img;
    Image cactus3Img;
    Image birdImg;

    class Block {
        int x;
        int y;
        int width;
        int height;
        Image img;

        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    //dinosaur
    int dinosaurWidth = 88;
    int dinosaurHeight = 94;
    int dinosaurX = 50;
    int dinosaurY = boardHeight - dinosaurHeight;

    Block dinosaur;

    //dinosaur Crouch
    int dinosaurWidthCrouch = 95;
    int dinosaurHeightCrouch = 60;
    int dinosaurYCrouch = boardHeight - dinosaurHeightCrouch;
    boolean isCrouched = false;

    //cactus
    int cactus1Width = 34;
    int cactus2Width = 69;
    int cactus3Width = 102;

    int cactusHeight = 70;
    int cactusX = 700;
    int cactusY = boardHeight - cactusHeight;
    ArrayList<Block> cactusArray;

    //bird
    int birdWidth = 85;
    int birdHeight = 50;
    int birdX = 700;

    //physics
    int velocityY = 0;
    int gravity = 1;
    int velocityX = -12;

    boolean gameOver = false;
    int score = 0;

    Timer gameLoop;
    Timer placeCactusTimer;

    public ChromeDinosaur() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.lightGray);
        setFocusable(true);
        addKeyListener(this);

        dinosaurImg = new ImageIcon(getClass().getResource("./img/dino-run.gif")).getImage();
        dinosaurDeadImg = new ImageIcon(getClass().getResource("./img/dino-dead.png")).getImage();
        dinosaurJumpImg = new ImageIcon(getClass().getResource("./img/dino-jump.png")).getImage();
        dinosaurDuckImg = new ImageIcon(getClass().getResource("./img/dino-duck.gif")).getImage();
        cactus1Img = new ImageIcon(getClass().getResource("./img/cactus1.png")).getImage();
        cactus2Img = new ImageIcon(getClass().getResource("./img/cactus2.png")).getImage();
        cactus3Img = new ImageIcon(getClass().getResource("./img/cactus3.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./img/bird.gif")).getImage();

        //dinosaur
        dinosaur = new Block(dinosaurX, dinosaurY, dinosaurWidth, dinosaurHeight, dinosaurImg);
        //cactus
        cactusArray = new ArrayList<Block>();

        //game timer 
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
        
        //place cactus timer
        placeCactusTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeCactus();
            }
        });
        placeCactusTimer.start();
    }

    void placeCactus() {
        if (gameOver) {
            return;
        }

        double placeCactusChance = Math.random();
        if (placeCactusChance > .90) {
            Block cactus = new Block(cactusX, cactusY, cactus3Width, cactusHeight, cactus3Img);
            cactusArray.add(cactus);
        }
        else if (placeCactusChance > .70) {
            Block cactus = new Block(cactusX, cactusY, cactus2Width, cactusHeight, cactus2Img);
            cactusArray.add(cactus);
        }
        else if (placeCactusChance > .50) {
            Block cactus = new Block(cactusX, cactusY, cactus1Width, cactusHeight, cactus1Img);
            cactusArray.add(cactus);
        }
        else if (placeCactusChance > .30) {
            double birdYChance = (Math.random() * 200);
            int birdFly = (int) birdYChance;
            Block cactus = new Block(birdX, birdFly, birdWidth, birdHeight, birdImg);
            cactusArray.add(cactus);
        }

        if (cactusArray.size() > 10) {
            cactusArray.remove(0);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        //dinosaur
        g.drawImage(dinosaur.img, dinosaur.x, dinosaur.y, dinosaur.width, dinosaur.height, null);

        //cactus
        for (int i = 0; i < cactusArray.size(); i++) {
            Block cactus = cactusArray.get(i);
            g.drawImage(cactus.img, cactus.x, cactus.y, cactus.width, cactus.height, null);
        }

        //score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Courier", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), 10, 35);
        }
        else {
            g.drawString(String.valueOf(score), 10, 35);
        }
    }

    public void move() {
        //dinosaur
        velocityY += gravity;
        dinosaur.y += velocityY;

        if (dinosaur.y > dinosaurY) { //stops dino from passing through ground
            if (isCrouched) {
                dinosaur.y = dinosaurYCrouch;
                velocityY = 0;
                dinosaur.img = dinosaurDuckImg;
            }
            else {
                dinosaur.y = dinosaurY;
                velocityY = 0;
                dinosaur.img = dinosaurImg;
            }
        }

        for (int i = 0; i < cactusArray.size(); i++) {
            Block cactus = cactusArray.get(i);
            cactus.x += velocityX;

            if (collision(dinosaur, cactus)) {
                gameOver = true;
                dinosaur.img = dinosaurDeadImg;
            }
        }

        //score
        score++;
    }

    boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placeCactusTimer.stop();
            gameLoop.stop();
            dinosaur.height = dinosaurHeight;
            dinosaur.width = dinosaurWidth;
            if (isCrouched) {
                dinosaur.y = dinosaurY;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (dinosaur.y == dinosaurY) {
                velocityY = -17;
                dinosaur.img = dinosaurJumpImg;
            }

            if (gameOver) {
                //reset the game
                dinosaur.y = dinosaurY;
                dinosaur.img = dinosaurImg;
                velocityY = 0;
                cactusArray.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placeCactusTimer.start();
                isCrouched = false;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (dinosaur.y == dinosaurY) {
                velocityY = -17;
                dinosaur.img = dinosaurJumpImg;
            }
            if (gameOver) {
                //reset the game
                dinosaur.y = dinosaurY;
                dinosaur.img = dinosaurImg;
                velocityY = 0;
                cactusArray.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placeCactusTimer.start();
                isCrouched = false;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (dinosaur.y == dinosaurY) {
                isCrouched = true;
                dinosaur.y = dinosaurYCrouch;
                dinosaur.img = dinosaurDuckImg;
                dinosaur.height = dinosaurHeightCrouch;
                dinosaur.width = dinosaurWidthCrouch;
            } 
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (dinosaur.y == dinosaurYCrouch) {
                isCrouched = false;
                dinosaur.y = dinosaurY;
                dinosaur.img = dinosaurImg;
                dinosaur.height = dinosaurHeight;
                dinosaur.width = dinosaurWidth;
            }
        }
    }
}