
//required import statements
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Tetris extends JPanel {

	//set the initial width and height of your image
	private static final int WIDTH = 800;
	private static final int HEIGHT = 1000;
	private static int size = 40;

	//required global variables (initialize these in the constructor) 
	private BufferedImage image;
	private Graphics g;
	public Timer timer;
	public static Block currentBlock;
	public static Block heldBlock;
	public static Color[][] matrix;
	public static int score;
	public static int linescleared;
	public static boolean fastFalling;
	public static ArrayList<Block> queue;
	//change this to whatever object(s) you are animating
	public int deltaTime=10;
	public int currentTime=0;
	public int dropTime=200;
	
	//Constructor required by BufferedImage

	//sound hell
	public AudioFormat audioFormat;
    public AudioInputStream audioInputStream;
    public Clip clip;
	public Tetris() {
		//set up Buffered Image and Graphics objects
		image =  new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = image.getGraphics();
		g.setColor(Color.black.brighter());
		g.fillRect(0, 0, WIDTH, HEIGHT);
		matrix = new Color[20][10];
		score = 0;
		currentBlock = new SquareBlock();
		queue = new ArrayList<Block>();
		for(int i=0;i<5;i++) {
			randomBlock();
		}
		Clip line;
		File soundFile = new File("Tetris.wav");
		try {
		audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch(IOException e) {
			//shit.
		} catch(UnsupportedAudioFileException e) {
			//whoops.
		}
		audioFormat = audioInputStream.getFormat();
		System.out.println(audioFormat);
		DataLine.Info info = new DataLine.Info(Clip.class, audioFormat); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
		// Handling the error.
		}
		// Obtain and open the line.
		try {
			line = (Clip) AudioSystem.getLine(info);
			line.open(audioInputStream);
			line.start();
		} catch (LineUnavailableException ex) {
			
		//... 
		} catch (IOException e) {
			//i have no clue how to do this.
		}
		


		//set up and start the Timer
		timer = new Timer(deltaTime, new TimerListener());
		timer.start();
		addKeyListener(new Keyboard());
		setFocusable(true);


	}
	private class Keyboard implements KeyListener {
		public void keyPressed(KeyEvent e) { 
			if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
				if(canMove(true)) {
					currentBlock.setxLocation(currentBlock.getxLocation()+1);
				}


			} else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
				if(canMove(false)) {
					currentBlock.setxLocation(currentBlock.getxLocation()-1);
				}

			
			} else if(e.getKeyCode() == KeyEvent.VK_UP) {
				currentBlock.rotateRight();
			} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				hardDrop();
			} else if(e.getKeyCode() == KeyEvent.VK_C) {
				if(heldBlock==null) {
					heldBlock=currentBlock;
					heldBlock.setxLocation(5);
					heldBlock.setyLocation(0);
					currentBlock=queue.remove(0);
					randomBlock();

				} else {
					Block temp = heldBlock;
					heldBlock=currentBlock;
					heldBlock.setyLocation(0);
					heldBlock.setxLocation(5);
					currentBlock=temp;
				}
			}  
			if(e.getKeyCode() == KeyEvent.VK_DOWN) {
				fastFalling=true;
			}
			if(e.getKeyCode()== KeyEvent.VK_ESCAPE) {
				System.exit(0);
			}
			

		  //INCOMPLETE
		}
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode()==KeyEvent.VK_DOWN) {
				fastFalling=false;
			}
		 }
		public void keyTyped(KeyEvent e) { }
		}

	//TimerListener class that is called repeatedly by the timer
	private class TimerListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(currentTime%dropTime==0||(fastFalling&&currentTime%30==0)) {
				dropLogic();
			}
			g.setColor(Color.black.brighter());
			g.fillRect(0, 0, WIDTH, HEIGHT);
			//draws grid
			//vertical lines
			g.setColor(Color.white);
			for(int i=1; i<12;i++) {
				g.drawLine(i*size, size, i*size, size*21);
			}
			//horizontial lines
			g.drawLine(size, size, 200, size);
			for(int n=size;n<size*22;n=n+size) {
				g.drawLine(size, n, 11*size, n);
			}
			for(int i=0;i<matrix.length;i++) {
				for(int n=0;n<matrix[i].length;n++){
					if(!(matrix[i][n]==null)){
						drawBlock(g,(n+1)*size,(i+1)*size,matrix[i][n]);

					}
				}
			}
			currentBlock.draw(g,(currentBlock.getxLocation()+1)*size,(currentBlock.getyLocation()+1)*size,size);
			g.setColor(Color.white);
			g.setFont(new Font("Impact",Font.PLAIN,40));
			g.drawString(score+"", 0, size);


			if(!(heldBlock==null)) {
				heldBlock.draw(g, 650, 100, size);
			}
			for(int i=0;i<queue.size();i++) {
				queue.get(i).draw(g, 500, 100+150*i, size);
			}



			repaint();
			currentTime+=deltaTime;
			if(currentTime>1000*20&&currentTime<1000*40) {
				dropTime=150;
			}
		}
		
	}
	public static void hardDrop() {
		while(!isTouching()) {
			currentBlock.setyLocation(currentBlock.getyLocation()+1);
		}	
		onContact();
		


	}
	public static void randomBlock() {
		int rando=(int)(Math.random()*7);
		if(rando==0) {
			queue.add(new SquareBlock());
		} else if(rando==1) {
			queue.add(new TBlock());
		} else if(rando==2) {
			queue.add(new ZBlock());
		} else if(rando==3) {
			queue.add(new BLBlock());
		} else if(rando==4) {
			queue.add(new LBlock());
		} else if(rando==5) {
			queue.add(new LongBlock());
		} else {
			queue.add(new SBlock());
		}
	}
	public static boolean canMove(boolean isRight) {
		if(isRight){
			int[][] state = currentBlock.getCurrentStateMap();
			for(int i=0;i<state.length;i++) {
				for(int n=0;n<state[i].length;n++){
					if(state[i][n]==1){
						if((currentBlock.getxLocation()+n+1)==matrix[0].length||!(matrix[currentBlock.getyLocation()+i][currentBlock.getxLocation()+n+1]==null)) {
							return false;
							
						} 
					}
				}
			}
			return true;
		}
		else {
			int[][] state = currentBlock.getCurrentStateMap();
			for(int i=0;i<state.length;i++) {
				for(int n=0;n<state[i].length;n++){
					if(state[i][n]==1){
						if((currentBlock.getxLocation()+n)==0||!(matrix[currentBlock.getyLocation()+i][currentBlock.getxLocation()+n-1]==null)) {
							return false;
							
						} 
					}
				}
			}
		}
		return true;
		
	}

	//do not modify this
	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
	}

	//main method with standard graphics code
	public static void main(String[] args) {
		JFrame frame = new JFrame("tetris!");
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocation(0, 0);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new Tetris()); 
		frame.setVisible(true);
	}
	//to be called on a drop frame
	public static void dropLogic() {
		if(isTouching()) {
			onContact();
		} else {
			currentBlock.setyLocation(currentBlock.getyLocation()+1);

		}
	}
	//determines whether the block will hit something when it goes down
	public static boolean isTouching() {
		int[][] state = currentBlock.getCurrentStateMap();
        for(int i=0;i<state.length;i++) {
            for(int n=0;n<state[i].length;n++){
                if(state[i][n]==1){
					//if the spot in the matrix directly below the block is occupied, this returns a true for isTouching
					//if the block is touching the ground, it returns true
                    if((currentBlock.getyLocation()+i+1)==matrix.length) {
						return true;
						
					} else if(!(matrix[currentBlock.getyLocation()+i+1][currentBlock.getxLocation()+n]==null)) {
						return true;
					}
                }
            }
        }
		return false;
	}

	//to be called when a block collides with the ground
	public static void onContact() {
		//turn the current block into spots in the matrix
		int[][] state = currentBlock.getCurrentStateMap();
        for(int i=0;i<state.length;i++) {
            for(int n=0;n<state[i].length;n++){
                if(state[i][n]==1){
					//if the spot in the matrix directly below the block is occupied, this returns a true for isTouching
					//if the block is touching the ground, it returns true
                    matrix[currentBlock.getyLocation()+i][currentBlock.getxLocation()+n]=currentBlock.getColor();


					
                }
            }
        }
		fastFalling=false;
		//check to see if there's any cleared lines
		//linescleared is amount of cleared lines
		int currentlinescleared=0;
		for(int i=0;i<matrix.length;i++) {
			//checks to see if the line is complete
			boolean nonull=true;
			for(int n=0;n<matrix[i].length;n++) {
				if(matrix[i][n]==null) {
					nonull=false;
				}
			}
			if(nonull) {
				currentlinescleared++;
				moveDown(i);
			}
		}
		//move the queue into the current block
		currentBlock = queue.get(0);
		queue.remove(0);
		randomBlock();
		//add something to the queue
		linescleared+=currentlinescleared;
		score=score+currentlinescleared;
	}
	public static void drawBlock(Graphics g, int x, int y, Color color) {
		g.setColor(color);
		g.fillRect(x, y, size, size);
		g.setColor(Color.black);
		g.drawLine(x, y, x+size, y);
		g.drawLine(x+size, y, x+size, y+size);
		g.drawLine(x, y, x, y+size);
		g.drawLine(x, y+size, x+size, y+size);


	}
	//call to clear a line and move the ones above it down one
	public static void moveDown(int line) {
		//clears the current line
		for(int n=0;n<matrix[line].length;n++) {
			matrix[line][n]=null;
		}
		//moves each line above it down one
		for(int i=line;i>0;i--) {
			for(int n=0;n<matrix[i].length;n++){
				matrix[i][n]=matrix[i-1][n];
			}

		}
		//clears the top line
		for(int n=0;n<matrix[0].length;n++){
			matrix[0][n]=null;
		}
	}
	
}