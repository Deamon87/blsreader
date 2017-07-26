package com.wow.blsreader;

import com.peterfranza.LittleEndianDataInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by Deamon on 23/11/2015.
 */
public class BlsReaderMain {

    public static String combine(String[] split, int length) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < length; i++){
            stringBuffer.append(split[i]);
            stringBuffer.append(".");
        }
        stringBuffer.setLength(stringBuffer.length()-1);

        return stringBuffer.toString();
    }

    public static void read16Record(int offset, int size, LittleEndianDataInputStream istream) throws IOException {
        istream.reset();
        istream.skipBytes(offset);

        int[] unkrec1 = new int[size];
        int[] unkrec2 = new int[size];
        int[] unkrec3 = new int[size];
        int[] unkrec4 = new int[size];

        for (int i = 0; i < size; i++) {
            unkrec1[i] = istream.readInt();
            unkrec2[i] = istream.readInt();
            unkrec3[i] = istream.readInt();
            unkrec4[i] = istream.readInt();
        }

        for (int i = 0; i < size; i++) {
            istream.reset();
            istream.skipBytes(unkrec1[i]);
            String param1 = istream.readCString();

            istream.reset();
            istream.skipBytes(unkrec3[i]);
            String param2 = istream.readCString();
            //System.out.println("param1= "+param1+" param2= "+param2);
        }
    }
    public static void readUniformBuffer(int offset, int size, LittleEndianDataInputStream istream) throws IOException {
        istream.reset();
        istream.skipBytes(offset);

        int[] unkrec1 = new int[size];
        int[] unkrec2 = new int[size];
        int[] unkrec3 = new int[size];

        for (int i = 0; i < size; i++) {
            unkrec1[i] = istream.readInt();
            unkrec2[i] = istream.readInt();
            unkrec3[i] = istream.readInt();
        }

        for (int i = 0; i < size; i++) {
            istream.reset();
            istream.skipBytes(unkrec1[i]);
            String param1 = istream.readCString();

            //System.out.println("uniformBuffer"+i+"= "+param1);
        }
    }
    public static void readTextUniforms(int offset, int size, LittleEndianDataInputStream istream) throws IOException {
        istream.reset();
        istream.skipBytes(offset);

        int[] unkrec1 = new int[size];
        int[] unkrec2 = new int[size];
        int[] unkrec3 = new int[size];
        int[] unkrec4 = new int[size];

        for (int i = 0; i < size; i++) {
            unkrec1[i] = istream.readInt();
            unkrec2[i] = istream.readInt();
            unkrec3[i] = istream.readInt();
            unkrec4[i] = istream.readInt();
        }

        for (int i = 0; i < size; i++) {
            istream.reset();
            istream.skipBytes(unkrec1[i]);
            String param1 = istream.readCString();

            //System.out.println("textUniform"+i+"= "+param1);
        }
    }

    public static boolean seekGLS3(LittleEndianDataInputStream istream) throws IOException {
        istream.mark(8192);
        int j = 0;
        int len = istream.readInt();
        char[] magic = new char[4];
        magic[0] = (char) istream.readByte();
        magic[1] = (char) istream.readByte();
        magic[2] = (char) istream.readByte();
        magic[3] = (char) istream.readByte();
        while (istream.available()>0 && magic[0]!='3' && magic[1]!='S' && magic[2]!='L' && magic[3]!='G') {
            magic[0] = magic[1];
            magic[1] = magic[2];
            magic[2] = magic[3];
            magic[3] = (char) istream.readByte();
            j++;
        }

        if (istream.available()>0) {
            istream.reset();
            istream.skipBytes(j);
            return true;
        }

        return false;
    }

    public static void unpackShaders(String unpackedFileName, int shaders, String template) throws IOException {
        FileInputStream file = new FileInputStream(unpackedFileName);
        //BufferedInputStream bufferedInputStream= new BufferedInputStream(file);
        RandomAccessFile randomAccessFile = new RandomAccessFile(unpackedFileName, "r");
        LittleEndianDataInputStream istream = new LittleEndianDataInputStream(randomAccessFile);
        istream.mark(0);

        int i = 0;
        while (istream.available() > 0){
             //BLSBlock
             int flags = istream.readInt();
             int flags2 = istream.readInt();
             int unknown = istream.readInt();
             int unknown2 = istream.readInt();
             int unknown3 = istream.readInt();
             int unknown4 = istream.readInt();
             int unknown5 = istream.readInt();
            if (!seekGLS3(istream)) break;
            int len = istream.readInt();



             //GLS3 block

             istream.mark(len);
             char[] magic = new char[4];
             magic[0] = (char) istream.readByte();
             magic[1] = (char) istream.readByte();
             magic[2] = (char) istream.readByte();
             magic[3] = (char) istream.readByte();
             int size = istream.readInt();
             int type2 = istream.readInt(); // ?, 3 = GLSL
             int unk = istream.readInt(); // 0
             int target = istream.readInt(); // as in GL_FRAGMENT_SHADER // 8b31
             int codeOffset = istream.readInt();
             int codeSize = istream.readInt();
             int unk2 = istream.readInt(); //0
             int unk3 = istream.readInt(); //-1

             int inputParamOffset = istream.readInt();
             int inputParamSize = istream.readInt();      //sizeof record 16

             int outputOffset = istream.readInt();
             int outputSize = istream.readInt();     //sizeof record 16

             int uniformBufferOffset = istream.readInt();
             int uniformBufferSize = istream.readInt();     //sizeof record 12

             int textUniforms = istream.readInt();
             int textUniformsSize = istream.readInt();     //sizeof record 16

             int unk5Offset = istream.readInt();
             int unk5Size = istream.readInt();

             int unk6Offset = istream.readInt();
             int unk6Size = istream.readInt();

             int variableStringsOffset = istream.readInt();
             int variableStringsSize = istream.readInt();

             istream.reset();
             istream.skipBytes(codeOffset);

             String glslCode = istream.readCString(codeSize);

             FileOutputStream fileOut = new FileOutputStream(template+"/"+ i++ +".glsl");
             fileOut.write(glslCode.getBytes(StandardCharsets.UTF_8));
             fileOut.close();

             read16Record(inputParamOffset, inputParamSize, istream);
             read16Record(outputOffset, outputSize, istream);
             readUniformBuffer(uniformBufferOffset, uniformBufferSize, istream);
             readTextUniforms(textUniforms, textUniformsSize, istream);

             istream.reset();
             istream.skipBytes(len);

             System.gc();
         }

        istream.close();
    }

    public static void unpackBlsFile(String fileName) throws IOException, DataFormatException {
        String split[] = fileName.split("\\.");
        if (split.length < 2 || !split[split.length-1].equals("bls")) return;


        //FileInputStream file = new FileInputStream(fileName);
        RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "r");
        //BufferedInputStream bufferedInputStream= new BufferedInputStream(file);
        LittleEndianDataInputStream istream = new LittleEndianDataInputStream(randomAccessFile);
        istream.mark(0);

        char[] magic = new char[4];
        magic[0] = (char) istream.readByte();
        magic[1] = (char) istream.readByte();
        magic[2] = (char) istream.readByte();
        magic[3] = (char) istream.readByte();

        int version = istream.readInt();
        int nShaders = istream.readInt();
        int unk = istream.readInt();


        //istream.skipBytes(16);

        int ofsCompressedChunks = istream.readInt();
        int nCompressedChunks = istream.readInt();
        int ofsCompressedData = istream.readInt();

        String fileDirectory = combine(split, split.length - 1);
        String outFileName = fileDirectory + "/fullSteam.decomp";

        File outFileD = new File(fileDirectory);
        outFileD.mkdirs();

        FileOutputStream fileOut = new FileOutputStream(outFileName);

        int compressedChunksOffsets[] = new int[nCompressedChunks];

        istream.reset();
        istream.skipBytes(ofsCompressedChunks);
        for (int i =0; i < nCompressedChunks; i++) {
            compressedChunksOffsets[i] =istream.readInt();
        }

        for (int i =0; i < nCompressedChunks; i++) {
            istream.reset();

            int length;
            if (i != nCompressedChunks-1) {
                length = compressedChunksOffsets[i+1] - compressedChunksOffsets[i];
            } else {
                length = istream.available();
            }
            istream.mark(ofsCompressedData+compressedChunksOffsets[i]+length);
            istream.skipBytes(ofsCompressedData + compressedChunksOffsets[i]);


            byte [] readBuffer = new byte[length];
            istream.read(readBuffer, 0, length);


            Inflater decompresser = new Inflater();
            decompresser.setInput(readBuffer, 0, length);
            byte[] result = new byte[100];

            int resultLength = decompresser.inflate(result);
            while (resultLength > 0) {

                byte[] resultToWrite;
                if (resultLength < 100) {
                    resultToWrite = new byte[resultLength];
                    System.arraycopy(result, 0, resultToWrite, 0, resultLength);
                } else {
                    resultToWrite = result;
                }

                fileOut.write(resultToWrite);

                resultLength = decompresser.inflate(result);
            }
            decompresser.end();
        }
        fileOut.close();

        unpackShaders(outFileName, nShaders, fileDirectory);
    }

    public static void main(String[] args) throws IOException, DataFormatException {
        Stream.concat(
            Stream.concat(
                    Files.walk(Paths.get("d:\\Games\\wow_shaders\\21846\\shaders\\pixel/glfs_420/")),
                    Files.walk(Paths.get("d:\\Games\\wow_shaders\\21846\\shaders\\vertex/glvs_420/"))
            ),
            Stream.concat(
                    Files.walk(Paths.get("d:\\Games\\wow_shaders\\21846\\shaders\\geometry/glgs_420/")),
                    Files.walk(Paths.get("d:\\Games\\wow_shaders\\21846\\shaders\\geometry/glgs_420/"))
            )
        ).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                try {
                    unpackBlsFile(filePath.toString());
                } catch (Exception ignored) {
                    //System.out.print(ignored.getMessage());
                }

                System.gc();
            }
        });
    }
}












