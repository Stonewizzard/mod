package com.example.examplemod;

/** Shared hit regions and slot coordinates for the compact excavation screen. */
public final class ExcavationLayout {
    public static final int WIDTH=320,HEIGHT=236,CELL=10,BOARD_X=20,BOARD_Y=72;
    public static final int RECOVER_X=257,RECOVER_Y=214,RECOVER_WIDTH=50,RECOVER_HEIGHT=14;
    public static boolean recoveryAt(double x,double y){return inside(x,y,RECOVER_X,RECOVER_Y,RECOVER_WIDTH,RECOVER_HEIGHT);}
    public static final int[][] SLOTS={{26,40},{169,39},{207,39},{245,39},{283,39},{65,40}};
    public static int cellAt(double x,double y){
        int col=(int)Math.floor((x-BOARD_X)/CELL),row=(int)Math.floor((y-BOARD_Y)/CELL);
        return col>=0 && col<ExcavationStudy.COLS && row>=0 && row<ExcavationStudy.ROWS?row*ExcavationStudy.COLS+col:-1;
    }
    public static boolean inside(double x,double y,int left,int top,int width,int height){return x>=left && x<left+width && y>=top && y<top+height;}
    public static int toolAt(double x,double y){
        for(int i=1;i<=4;i++)if(inside(x,y,159+(i-1)*38,55,36,12))return i;
        return 0;
    }
}

