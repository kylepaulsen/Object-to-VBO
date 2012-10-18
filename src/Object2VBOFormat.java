import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Object2VBOFormat {
  public static boolean INVERT_TEXTURE_COORD_V = true;
  
  public String filename;
  public ArrayList<float[]> vertices;
  public ArrayList<float[]> normals;
  public ArrayList<float[]> textureCoords;
  public ArrayList<float[]> strides;
  public int hasNormals = -1;
  public int hasTextureCoords = -1;
  public int strideSize = 0;
  public int numVertices = 0;
  public int numTextureCoords = 0;
  public int numNormals = 0;
  
  public Object2VBOFormat(String filename){
    this.filename = filename;
    this.vertices = new ArrayList<float[]>();
    this.normals = new ArrayList<float[]>();
    this.textureCoords = new ArrayList<float[]>();
    this.strides = new ArrayList<float[]>();
    this.readFile();
  }
  
  // A good potion of this code was borrowed from here:
  // http://potatoland.org/code/gl/source/glmodel/
  private void readFile(){
    try {
      String line = "";
      BufferedReader reader = new BufferedReader(new FileReader(this.filename));
      while ((line = reader.readLine()) != null) {
        // remove extra whitespace
        line = line.trim();
        line = line.replaceAll("  ", " ");
        //System.out.println("== "+line);
        if (line.length() > 0) {
          if (line.startsWith("v ")) {
            // vertex coord line looks like: v 2.628657 -5.257312 8.090169 [optional W value]
            vertices.add(read3Floats(line));
          }
          else if (line.startsWith("vt")) {
            // texture coord line looks like: vt 0.187254 0.276553 0.000000
            textureCoords.add(readTextureCoords(line));
          }
          else if (line.startsWith("vn")) {
            // normal line looks like: vn 0.083837 0.962494 -0.258024
            normals.add(read3Floats(line));
          }
          else if (line.startsWith("f ")) {
            // Face line looks like: f 1/3/1 13/20/13 16/29/16
            readFace(line);
            // assign material ID to polygon
            /*f.materialID = materialID;
            faces.add(f);           // add to complete face list
            group.faces.add(f);     // aad3Flad3Flad3Fldd to current group
            group.numTriangles += f.numTriangles(); // track number of triangles in group
            */
          }
          /*else if (line.startsWith("g ")) {
              // Group line looks like: g someGroupName
              String groupname = (line.length()>1)? line.substring(2).trim() : "";
              // "select" the given group
              group = findGroup(groupname);
              // not found: start new group
              if (group == null) {
                  group = new Group(groupname);
                  group.materialname = materialName;  // assign current material to new group
                  group.materialID = materialID;
                  groups.add(group);
              }
          }else if (line.startsWith("usemtl")) {
              // material line: usemtl materialName
              materialName = line.substring(7).trim();
              // lookup material name in libe
              materialID = (materialLib == null)? -1 : materialLib.findID(materialName);
              // assign material to current group
              group.materialname = materialName;
              group.materialID = materialID;
              //System.out.println("got usemtl " +group.name + ".materialname now is " + materialName);
          }
          else if (line.startsWith("mtllib")) {
              // material library line: mtllib materialLibeFile.mtl
              materialLibeName = line.substring(7).trim();
              if (materialLibeName.startsWith("./")) {
                  materialLibeName = materialLibeName.substring(2);
              }
              // load material library
              materialLib = new GLMaterialLib(filepath + materialLibeName);
          }
          */
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void convert(String filepath){
    try {
      FileOutputStream fos = new FileOutputStream(filepath);
      DataOutputStream dos = new DataOutputStream(fos);
      
      int numStrides = strides.size();
      
      //write file headers
      dos.writeChar('s');
      dos.writeInt(strideSize);
      dos.writeChar('v');
      dos.writeInt(numStrides);
      dos.writeChar('n');
      dos.writeInt(hasNormals*3);
      dos.writeChar('t');
      dos.writeInt(hasTextureCoords*2);
      dos.writeChar('\n');
      
      //start writing stride data.
      for(int t=0; t<numStrides; ++t){
        float[] stride = strides.get(t);
        for(int x=0; x<strideSize; ++x){
          dos.writeFloat(stride[x]);
        }
      }
      
      dos.close();
    } catch (IOException e) {
      System.out.println("IOException : " + e);
    }
  }
  
  /**
     * Parse three floats from the given input String.  Ignore the
     * first token (the line type identifier, ie. "v", "vn", "vt").
     * Return array: float[3].
     * @param line  contains line from OBJ file
     * @return array of 3 float values
     */
  private float[] read3Floats(String line) {
    String[] data = line.split(" ");
    if(data.length != 4){
      System.out.println("read3Floats(): error on line '" + line);
      return null;
    }
    return new float[] {Float.parseFloat(data[1]),
        Float.parseFloat(data[2]),
        Float.parseFloat(data[3])};
  }
  
  private float[] readTextureCoords(String line) {
    String[] data = line.split(" ");
    if(data.length != 4 && data.length != 3){
      System.out.println("read2Floats(): error on line '" + line);
      return null;
    }
    if(INVERT_TEXTURE_COORD_V){
      return new float[] {Float.parseFloat(data[1]),
          1.0f-Float.parseFloat(data[2])};
    }else{
      return new float[] {Float.parseFloat(data[1]),
          Float.parseFloat(data[2])};
    }
  }
  
  /**
   * Read a face definition from line and construct a stride.
   * Face line looks like: f 1/3/1 13/20/13 16/29/16
     * Three or more sets of numbers, each set contains vert/txtr/norm
     * references.  A reference is an index into the vert or txtr
     * or normal list.
   * @param line   string from OBJ file with face definition
   * @return       Face object
   */
  private void readFace(String line) {
    String[] triplets = line.split(" ");
    if(triplets.length != 4){
      System.out.println("readFace(): error on line '" + line);
      return;
    }
        
    String[] triplet1 = triplets[1].split("/", -1);
    String[] triplet2 = triplets[2].split("/", -1);
    String[] triplet3 = triplets[3].split("/", -1);
    
    if(hasNormals == -1 || hasTextureCoords == -1){
      // this is the first time readFace was called,
      // set some important vars.
      strideSize = 3;
      numVertices = vertices.size();
      numNormals = normals.size();
      numTextureCoords = textureCoords.size();
      // triplets look like 13/20/13 and hold
        // vert/txtr/norm indices. If no texture coord has been
        // assigned, may be 13//13. check for empty string.
      if(triplet1[1].length() == 0){
        hasTextureCoords = 0;
      }else{
        hasTextureCoords = 1;
        strideSize += 2;
      }
      if(triplet1[2].length() == 0){
        hasNormals = 0;
      }else{
        hasNormals = 1;
        strideSize += 3;
      }
    }
    
    //construct 3 strides from the face data, one for each triplet.
    
    int index = 3;
    float[] newStride = new float[strideSize];
    float[] vertexCoord = vertices.get(convertIndex(Integer.parseInt(triplet1[0]), numVertices));
    newStride[0] = vertexCoord[0];
    newStride[1] = vertexCoord[1];
    newStride[2] = vertexCoord[2];
    if(hasNormals == 1){
      float[] normalCoord = normals.get(convertIndex(Integer.parseInt(triplet1[2]), numNormals));
      newStride[index] = normalCoord[0];
      newStride[index+1] = normalCoord[1];
      newStride[index+2] = normalCoord[2];
      index += 3;
    }
    if(hasTextureCoords == 1){
      float[] textureCoord = textureCoords.get(convertIndex(Integer.parseInt(triplet1[1]), numTextureCoords));
      newStride[index] = textureCoord[0];
      newStride[index+1] = textureCoord[1];
    }
    strides.add(newStride);
    index = 3;
    
    newStride = new float[strideSize];
    vertexCoord = vertices.get(convertIndex(Integer.parseInt(triplet2[0]), numVertices));
    newStride[0] = vertexCoord[0];
    newStride[1] = vertexCoord[1];
    newStride[2] = vertexCoord[2];
    if(hasNormals == 1){
      float[] normalCoord = normals.get(convertIndex(Integer.parseInt(triplet2[2]), numNormals));
      newStride[index] = normalCoord[0];
      newStride[index+1] = normalCoord[1];
      newStride[index+2] = normalCoord[2];
      index += 3;
    }
    if(hasTextureCoords == 1){
      float[] textureCoord = textureCoords.get(convertIndex(Integer.parseInt(triplet2[1]), numTextureCoords));
      newStride[index] = textureCoord[0];
      newStride[index+1] = textureCoord[1];
    }
    strides.add(newStride);
    index = 3;
    
    newStride = new float[strideSize];
    vertexCoord = vertices.get(convertIndex(Integer.parseInt(triplet3[0]), numVertices));
    newStride[0] = vertexCoord[0];
    newStride[1] = vertexCoord[1];
    newStride[2] = vertexCoord[2];
    if(hasNormals == 1){
      float[] normalCoord = normals.get(convertIndex(Integer.parseInt(triplet3[2]), numNormals));
      newStride[index] = normalCoord[0];
      newStride[index+1] = normalCoord[1];
      newStride[index+2] = normalCoord[2];
      index += 3;
    }
    if(hasTextureCoords == 1){
      float[] textureCoord = textureCoords.get(convertIndex(Integer.parseInt(triplet3[1]), numTextureCoords));
      newStride[index] = textureCoord[0];
      newStride[index+1] = textureCoord[1];
    }
    strides.add(newStride);
  }

  /**
   * Convert a vertex reference number into the correct vertex array index.
   * <BR>
   * Face definitions in the OBJ file refer to verts, texture coords and
   * normals using a reference number. The reference numbers is the position
   * of the vert in the vertex list, in the order read from the OBJ file.
   * Reference numbers start at 1, and can be negative (to refer back into
   * the vert list starting at the bottom, though this seems to be rare). The
   * same approach applies to texture coords and normals.
   * <BR>
   * This function converts reference numbers to an array index starting at 0,
   * and converts negative reference numbers to 0-N array indexes.
   * <BR>
   * @param index   the non 0 based index that was parsed.
   * @param numVerts   the number of floats read into the corresponding arraylist.
   * @return idx    will be 0 - N index into vert array
   */
  public int convertIndex(int index, int numVerts) {
    // OBJ file index starts at 1 so convert index to start at 0
    return (index < 0) ? (numVerts + index) : index-1;
  }
}
