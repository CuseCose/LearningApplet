import com.sun.javafx.geom.Vec2d;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Main extends Applet implements Runnable, KeyListener {

    //BASIC VARIABLES
    private final int WIDTH=1280, HEIGHT=900;

    //GRAPHICS OBJECTS
    private Thread thread;
    Graphics gfx;
    Image img;


    //COLORS
    Color background=new Color(255, 255, 255);
    Color gridColor=new Color(0);
    Color goalColor=new Color(255,0,0);


    int timeWitho2;
    float[] goal;

    ArrayList<float[]> oldlocs;
    float[] loc;
    float speed=7;
    float o=0;//orientation
    float o1=0;//rate of change
    float o2=0;//rate of change of rate of change

    public void init(){//STARTS THE PROGRAM
        this.resize(WIDTH, HEIGHT);
        this.addKeyListener(this);
        img=createImage(WIDTH,HEIGHT);
        gfx=img.getGraphics();
        thread=new Thread(this);
        thread.start();
        reset();
    }

    public void paint(Graphics g){
        //BACKGROUND
        gfx.setColor(background);//background
        gfx.fillRect(0,0,WIDTH,HEIGHT);//background size
        renderRope(gfx);
        gfx.drawString("o = "+o+", o1 = "+o1+", o2 = "+o2, 100,100);
        gfx.setColor(goalColor);
        gfx.fillRect((int)goal[0],(int)goal[1],10,10);

        //RENDER FOREGROUND


        //FINAL
        g.drawImage(img,0,0,this);
    }

    public void renderRope(Graphics g){
        g.setColor(gridColor);
        for (int i=0; i<oldlocs.size(); i++){
            g.fillRect((int)oldlocs.get(i)[0],(int)oldlocs.get(i)[1],5,5);
        }
    }

    public void update(Graphics g){ //REDRAWS FRAME

        paint(g);
    }

    public void move(){
        float dor=(float)(Math.atan((goal[1]-loc[1])/(goal[0]-loc[0])));
        if ((goal[0]-loc[0])<0){ dor=dor+3.14f; }

        //System.out.println("optimal dir = "+Math.toDegrees(dor));

        o=o%6.28f;

        timeWitho2--;
        if (timeWitho2<1){
            resetO2();
        }
        o=o+o1;
        if (Math.abs(o1+o2)<3.14f*3/32) {
            o1 = o1 + o2;
        }else {
            resetO2();
        }
        oldlocs.add(loc);
        //o=dor;

        loc=new float[]{loc[0]+(float)(Math.cos(o)*speed),loc[1]+(float)(Math.sin(o)*speed)};
    }
    public void resetO2(){
        float dor=(float)(Math.atan((goal[1]-loc[1])/(goal[0]-loc[0])));
        if ((goal[0]-loc[0])<0){ dor=dor+3.14f; }

        float ddor=dor-o;//ddor = the change needed to get in line

        if (ddor>3.14){
            ddor=-3.14f+ddor;
        }

        float dddor=ddor-o1;

        o2=(float)(Math.random()*dddor/1);
        float rand=(float)(3.14/16*Math.random())-(3.14f/32);
        o2=(o2+rand)/2f;


        timeWitho2=(int)(Math.random()*15*Math.random())+1;
        //timeWitho2=(int)(Math.random()*(ddor/o2))+1;

    }



    private void finish(){
        long startt=System.currentTimeMillis();
        while (!hasReachedEnd()&&System.currentTimeMillis()<startt+2000){
            move();
            //moveO2ToDor();
        }
    }

    public boolean hasReachedEnd(){ return (Math.abs(loc[0]-goal[0])<20&&Math.abs(loc[1]-goal[1])<20); }



    public void reset(){
        //goal=new float[]{(float)(Math.random()*WIDTH*.66)+(WIDTH/6),(float)(Math.random()*HEIGHT*.66)+(HEIGHT/6)};
        //loc=new float[]{(float)(Math.random()*WIDTH*.66)+(WIDTH/6),(float)(Math.random()*HEIGHT*.66)+(HEIGHT/6)};

        //goal=new float[]{(float)-(Math.random()*WIDTH*.1)+(WIDTH/6),(float)(Math.random()*HEIGHT*.66)+(HEIGHT/6)};
        //loc=new float[]{(float)(Math.random()*WIDTH*.1)+(WIDTH*5/6),(float)(Math.random()*HEIGHT*.66)+(HEIGHT/6)};

        loc=new float[]{(float)(Math.random()*WIDTH*3/4)+(WIDTH/8),(float)-(Math.random()*HEIGHT*.1)+(HEIGHT/6)};
        goal=new float[]{(float)(Math.random()*WIDTH*3/4)+(WIDTH/8),(float)(Math.random()*HEIGHT*.1)+(HEIGHT*5/6)};

        timeWitho2=1;
        o=(float)(Math.random()*3.14);
        o1=(float)(Math.random()*3.14/10);
        o2=(float)(Math.random()*3.14/10);
        oldlocs=new ArrayList<>();
        oldlocs.add(loc);

    }

    public void run() { for (;;){//CALLS UPDATES AND REFRESHES THE GAME
            //UPDATES
            repaint();//UPDATES FRAME
            try{ Thread.sleep(15); } //ADDS TIME BETWEEN FRAMES (FPS)
            catch (InterruptedException e) { e.printStackTrace();System.out.println("GAME FAILED TO RUN"); }//TELLS USER IF GAME CRASHES AND WHY
    } }

    public void straighten(){
        int r=1;
        for (int i=r; i<oldlocs.size()-r;i++){
            float sumx=0;
            float sumy=0;
            for (int z=i-r; z<=i+r; z++){
                sumx+=oldlocs.get(z)[0];
                sumy+=oldlocs.get(z)[1];
            }
            sumx=sumx/(2*r+1);
            sumy=sumy/(2*r+1);
            oldlocs.set(i, new float[]{sumx,sumy});
        }
    }

    private void pullDown(){
        int r=3;
        float weight=.3f;
        for (int i=oldlocs.size()-1; i>0;i--){
            float sumx=0;
            float sumy=0;
            float miny=0;
            int amt=0;
            for (int z=i-r; z<=i+r; z++){
                if (z<1||z>=oldlocs.size()){continue;}
                amt++;
                sumx+=oldlocs.get(z)[0];
                sumy+=oldlocs.get(z)[1];
                if (oldlocs.get(z)[1]>miny){
                    miny=oldlocs.get(z)[1];
                }
            }
            sumx=sumx/amt;
            sumy=sumy/amt;
            sumy=(sumy+(weight*miny))/(1+weight);
            oldlocs.set(i, new float[]{oldlocs.get(i)[0],sumy});
        }
    }

    //INPUT
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_SPACE){
            move();
        }
        if (e.getKeyCode()==KeyEvent.VK_R){
            reset();
        }
        if (e.getKeyCode()==KeyEvent.VK_F){
            finish();
        }
        if (e.getKeyCode()==KeyEvent.VK_S){
            straighten();
        }
        if (e.getKeyCode()==KeyEvent.VK_G){
            pullDown();
        }
    }
    public void keyReleased(KeyEvent e) {

    }
    public void keyTyped(KeyEvent e) { }

    //QUICK METHOD I MADE TO DISPLAY A COORDINATE GRID

}