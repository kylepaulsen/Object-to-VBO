
public class Main {
  public static void main(String args[]){
    if(args.length != 2){
      System.out.println("Obj2VBO usage: Obj2VBO.jar in.obj out.vbo");
      return;
    }
    Object2VBOFormat loader = new Object2VBOFormat(args[0]);
    loader.convert(args[1]);
  }
}
