package graphbuilder;


import java.io.*;
import java.util.HashSet;

public class CreateNeo4jNodes {

        public static void main(String[] args) {
            File csvFile = new File("./allCourses.txt");
            BufferedReader br = null;
            Writer writer = null;
            Writer writer1 = null;
            String line = "";
            String cvsSplitBy = "\t\t";

            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("neo4jCourses.csv"), "utf-8"));
                writer1 = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("neo4jCourses.txt"), "utf-8"));
                br = new BufferedReader(new FileReader(csvFile));

                HashSet<String> names = new HashSet<>();

                String fileHead = "name,number,id,path,description,aliases,courseQuality,professorQuality,difficulty,amountLearned,workRequired,RecommendToMajor,RecommendToNonMajor,numberReviewers";
                writer.append(fileHead);

                while ((line = br.readLine()) != null) {
                    String[] courseInfo = line.split(cvsSplitBy);
                    String name = courseInfo[0].replaceAll(",", "").replaceAll("\"", "").trim();
                    if(courseInfo.length == 14 && !name.isEmpty() && !courseInfo[2].equals("id") && !names.contains(name)) {
                        StringBuilder sb = new StringBuilder();
                        writer.append("\n");
                        sb.append(name);
                        sb.append(',');
                        sb.append(courseInfo[1]);
                        sb.append(',');
                        sb.append(courseInfo[2]);
                        sb.append(',');
                        sb.append(courseInfo[3]);
                        sb.append(',');
                        sb.append(courseInfo[4].trim().replaceAll("\"", "").replaceAll(",", ""));
                        sb.append(',');
                        sb.append(courseInfo[5].replaceAll(",", " --"));
                        sb.append(',');
                        sb.append(courseInfo[6]);
                        sb.append(',');
                        sb.append(courseInfo[7]);
                        sb.append(',');
                        sb.append(courseInfo[8]);
                        sb.append(',');
                        sb.append(courseInfo[9]);
                        sb.append(',');
                        sb.append(courseInfo[10]);
                        sb.append(',');
                        sb.append(courseInfo[11]);
                        sb.append(',');
                        sb.append(courseInfo[12]);
                        sb.append(',');
                        sb.append(courseInfo[13]);
                        writer.append(sb.toString());
                        writer1.append(line);
                        writer1.append("\n");
                        names.add(name);
                    }

                }
                System.out.println("Number of unique nodes created for Neo4j: " + names.size());


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                        writer.flush();
                        writer.close();
                        writer1.flush();
                        writer1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

}
