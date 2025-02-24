package models;

import collision.AABB;
import collision.Collision;
import org.joml.Vector2f;
import org.joml.Vector3f;
import render.Animation;
import render.Camera;
import render.Shader;
import windows.Window;

import static org.lwjgl.glfw.GLFW.*;

public class Joueur {
    private double vie;
    private int attaque;
    private float posX;
    private float posY;
    private Model model;
    private Animation texture;
    private models.Transform tr;
    private int piecesCollectees;
    private Weapon w;

    private AABB boundingBox;

    public Joueur(int vie, int posX, int posY) {

        piecesCollectees=0;
        this.vie = vie;

        float[] vertices = new float[]{
                -1f, 1f, 0,//TOP LEFT     0
                1f, 1f, 0,//TOP RIGHT     1
                1f, -1f, 0,//BOTTOM RIGHT 2

                // -0.5f,0.5f,0,//TOP LEFT
                // 0.5f,-0.5f,0,//BOTTOM RIGHT
                -1, -1f, 0//BOTTOM LEFT  3

        };


        float[] texture = new float[]{
                0, 0,
                1, 0,
                1, 1,
                // 0,0,
                // 1,1,
                0, 1

        };


        int[] indices = new int[]{
                0, 1, 2,
                2, 3, 0
        };
        model = new Model(vertices, texture, indices);
        this.texture = new Animation(2, 3, "joueur");


        AABB box = null;

        tr = new Transform();
        tr.scale = new Vector3f(32, 32, 1);
        tr.pos.x = posX;
        tr.pos.y = posY;
        this.posX = posX;
        this.posY = posY;
        boundingBox = new AABB(new Vector2f(posX, posY), new Vector2f(1, 1));

    }

    public void setPos(int x, int y) {
        tr.pos.x = x;
        tr.pos.y = y;
        posX = tr.pos.x;
        posY = tr.pos.y;
    }


    public void deplacement(float delta, Window win, Camera camera, Labyrinthe world) {
        int x = (int) Math.ceil((posX) / 2 / 100);
        int y = (int) Math.ceil(Math.abs(posY) / 2 / 100);



        /*
        System.out.println("Pos x : "+x+" pos Y = "+y);

        if(posY+1 <= 0 && posX-1>=0) {
            Tile t = world.getElementPlateau((int) Math.ceil((posX )/2/100), (int) Math.ceil(Math.abs(posY )/2/100));

            if(t!=null)
                if(t.getId()!=0)
                    System.out.println("Je marche sur : " + t.getId());
        }

         */


        if (win.getInput().isKeyDown(GLFW_KEY_LEFT)) {
            tr.pos.add(new Vector3f(-10 * delta, 0, 0));
        }
        if (win.getInput().isKeyDown(GLFW_KEY_RIGHT)) {
            tr.pos.add(new Vector3f(10 * delta, 0, 0));
        }
        if (win.getInput().isKeyDown(GLFW_KEY_UP)) {
            tr.pos.add(new Vector3f(0, 10 * delta, 0));
        }
        if (win.getInput().isKeyDown(GLFW_KEY_DOWN)) {
            tr.pos.add(new Vector3f(0, -10 * delta, 0));
        }
        if (getW() != null)
            setPosArme();


        posX = tr.pos.x;
        posY = tr.pos.y;

        boundingBox.getCenter().set(posX, posY);

        AABB[] boxes = new AABB[25];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                boxes[i + j * 5] = world.verifierCollision((int) (((posX / 2) + 0.5f) - (5 / 2)) + i, (int) (((-posY / 2) + 0.5f) - (5 / 2)) + j);
                //  boxes[i+j*5]=world.verifierCollision((int)posX/3,(int)posY/3);

            }
        }

        AABB box = null;
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null) {
                if (box == null) {
                    box = boxes[i];
                }
                Vector2f length1 = box.getCenter().sub(posX, posY, new Vector2f());
                Vector2f length2 = boxes[i].getCenter().sub(posX, posY, new Vector2f());

                if (length1.lengthSquared() > length2.lengthSquared()) {
                    box = boxes[i];
                }
            }
        }

        if (box != null) {
            Collision data = boundingBox.getCollision(box);
            if (data.isIntersecting) {
                // System.out.println("collision");
                //System.out.println((int)(box.getCenter().x/2)+" ====" +(int)(box.getCenter().y/2));
                //System.out.println("Collision avec : "+world.getElementPlateau((int)(box.getCenter().x/2),(int)Math.abs(box.getCenter().y/2)).getId());
                //System.out.println("Box x:"+box.getCenter().x+" //// box y = "+box.getCenter().y);
                //System.out.println("Joueur x "+posX+" joueur y ="+posY);

                boundingBox.correctPosition(box, data);
                tr.pos.set(boundingBox.getCenter(), 0);
            }


        }
        //System.out.println("POS X du joueur : " + posX + "\nPOS X caméra : " +camera.getPosition().x + "\nwindows diviser par 2 : "+win.getWidth()/2);
        //if (posX>=camera.getPosition().x)
        camera.setPosition(tr.pos.mul(-world.getScale() /* /2f -> -16*/, new Vector3f()));
        if (w != null)
            w.setPos(posX, posY);

        //System.out.println("Joueur : "+getPosX()+","+getPosY()+" et weapon:"+w.getPosX()+","+w.getPosY());

    }


    public void render(Shader shader, Camera camera) {
        shader.bind();
        shader.setUniform("sampler", 0);
        shader.setUniform("projection", tr.getProjection(camera.getProjection()));
        texture.bind(0);
        model.render();


    }

    public double getVie() {
        return this.vie;
    }

    public void setVie(double vie) {
        this.vie = vie;
    }


    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public Weapon getW() {
        return w;
    }

    public void setW(Weapon w) {
        this.w = w;
    }

    public void setPosArme() {
        getW().setPos(((((getPosX() / 2) + 0.5f) - (5 / 2))) + 2, -((((getPosY() / 2) + 0.5f) - (5 / 2))) - 2);

    }

    public void animationAttaque() {
        w.attaque();
    }

    public void setAttaque(int atk) {
        w.setAttaqueDegat(atk);
    }

    public void setPiecesCollectees(){
        piecesCollectees++;
    }

    public int getPiecesCollectees(){
        return piecesCollectees;
    }
}
