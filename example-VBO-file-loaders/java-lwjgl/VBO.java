package kylesSimpleEngine;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class VBO {
  public FloatBuffer floatBuffer;
  public int bufferHandle = -1;
  public int numStrides;
  public int strideLengthInFloats;
  public int strideLengthInBytes;
  public int normalFloats;
  public int textureFloats;
  public String filename;
  
  public VBO(String filename){
    this.filename = filename;
  }
  
  public void readVBOFile(){
    if(this.bufferHandle > -1){
      deleteBuffer();
    }
    this.bufferHandle = GL15.glGenBuffers();
    try {
      FileInputStream fis = new FileInputStream(this.filename);
      DataInputStream dis = new DataInputStream(fis);
      
      //read file headers
      char headerTag = dis.readChar();
      int stride=0, numStrides=0, normalFloats=0, textureFloats=0;
      while(headerTag != '\n'){
        switch(headerTag){
          case 's':
            stride = dis.readInt();
            break;
          case 'v':
            numStrides = dis.readInt();
            break;
          case 'n':
            normalFloats = dis.readInt();
            break;
          case 't':
            textureFloats = dis.readInt();
            break;
        }
        headerTag = dis.readChar();
      }
      
      int numFloats = stride*numStrides;
      FloatBuffer data = BufferUtils.createFloatBuffer(numFloats);
      for(int x=0; x<numFloats; ++x){
        data.put(dis.readFloat());
      }
      data.flip();
        
      this.floatBuffer = data;
      this.numStrides = numStrides;
      this.strideLengthInFloats = stride;
      this.strideLengthInBytes = stride*4;
      this.normalFloats = normalFloats;
      this.textureFloats = textureFloats;
        
    } catch (IOException e) {
      System.out.println("Error with VBO file!");
    }
  }
  
  public void transferStaticData(){
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.bufferHandle);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.floatBuffer, GL15.GL_STATIC_DRAW);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
  }
  
  public void deleteBuffer(){
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.bufferHandle);
    GL15.glDeleteBuffers(this.bufferHandle);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
  }
  
  public void render(){
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.bufferHandle);
    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
    GL11.glVertexPointer(3, GL11.GL_FLOAT, this.strideLengthInBytes, 0);
    int byteOffset = 12;
    if(this.normalFloats > 0){
      GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
      GL11.glNormalPointer(GL11.GL_FLOAT, this.strideLengthInBytes, byteOffset);
      byteOffset += 12;
    }
    if(this.textureFloats > 0){
      GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
      GL11.glTexCoordPointer(2, GL11.GL_FLOAT, this.strideLengthInBytes, byteOffset);
    }
    
    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.numStrides);
    
    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
    GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
  }
}
