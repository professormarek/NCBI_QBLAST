import org.biojava.nbio.core.sequence.io.util.IOUtils;
import org.biojava.nbio.ws.alignment.qblast.BlastProgramEnum;
import org.biojava.nbio.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import org.biojava.nbio.ws.alignment.qblast.NCBIQBlastOutputProperties;
import org.biojava.nbio.ws.alignment.qblast.NCBIQBlastService;

import java.io.*;

/**
 * this class demosntrates how to perform a blast query remotely using the NCBI QBLAST service.
 * to access web services we need to include an additional JAR file (in addition to BioJava core)
 * it's called biojava-ws-4.0.0.jar
 */
public class BioJavaNCBIQBLAST {

    public static void main(String[] args) {
        //come up with an example sequence that we will pass to the blast service
        String mysterySequence = "PYRTWNYHGSYDVKPTGSASSLVNGVVRLLSKPWDTITNVTTMAMTDTTPFGQQRVF";
        //we will produce the output into a text file for later processing
        String outputFileName = "blast_results.xml";

        /*
        the NCBIQBlastService class encapsulates and abstracts away the complexity related
        to connecting to the ncbi web service
        let's create a reference to the blast service
         */
        NCBIQBlastService blastService = new NCBIQBlastService();
        /*
        before using the service we need to set up a few things...

        1) set alignment properties - the ones we will use here are just scratching the surface..
        there are many more available properties (see BioJava documentation)

        */
        NCBIQBlastAlignmentProperties alignmentProperties = new NCBIQBlastAlignmentProperties();
        // 2) choose or set the program
        alignmentProperties.setBlastProgram(BlastProgramEnum.blastp);
        // 3) choose or set the database that we will use for the query
        alignmentProperties.setBlastDatabase("swissprot");
        // 4) set the output options using the appropriate class (default choices for now)
        NCBIQBlastOutputProperties outputProperties = new NCBIQBlastOutputProperties();
        // 5) each request to the NCBI Blast services has a unique ID. Declare a string to store the ID
        String requestID = null;
        // 6) set up buffered file IO to store the results
        FileWriter writer = null;
        BufferedReader reader = null;

        //now it's time to actually send the alignment request
        try{
            requestID = blastService.sendAlignmentRequest(mysterySequence, alignmentProperties);

            /*
            now we patiently wait for a result from the blast service
            the method blastService.isReady() queries the remote web service
            don't forget to store the requestID - we did this above - so you can use it
            to determine whether the results are ready
             */
            while( !blastService.isReady(requestID)){
                System.out.println("Waiting for blast results.. sleeping for 10 seconds");
                Thread.sleep(10000);
            }

            System.out.println("We have results from NCBI QBlast!");

            //read the results and write them to a file
            InputStream inputStream = blastService.getAlignmentResults(requestID, outputProperties);
            //associate the input stream with the buffered reader we set up earlier
            reader = new BufferedReader(new InputStreamReader(inputStream));
            //write the results to a file
            File output = new File(outputFileName);
            writer = new FileWriter(output);
            String line = null;
            while(( line = reader.readLine()) != null ){
                writer.write(line + System.getProperty("line.separator"));
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //clean up our objects and references on NCBI's webservice
            IOUtils.close(writer);
            IOUtils.close(reader);
            //tell NCBI server that your results don't need to be stored anymore
            blastService.sendDeleteRequest(requestID);
        }

        System.out.println("Program completed successfully!");
    }








}
