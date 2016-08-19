package org.vitrivr.cineast.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.db.ADAMproSelector;
import org.vitrivr.cineast.core.features.AverageColor;
import org.vitrivr.cineast.core.features.AverageColorARP44;
import org.vitrivr.cineast.core.features.AverageColorCLD;
import org.vitrivr.cineast.core.features.AverageColorGrid8;
import org.vitrivr.cineast.core.features.AverageColorRaster;
import org.vitrivr.cineast.core.features.AverageFuzzyHist;
import org.vitrivr.cineast.core.features.CLD;
import org.vitrivr.cineast.core.features.ChromaGrid8;
import org.vitrivr.cineast.core.features.DominantColors;
import org.vitrivr.cineast.core.features.DominantEdgeGrid16;
import org.vitrivr.cineast.core.features.DominantEdgeGrid8;
import org.vitrivr.cineast.core.features.EHD;
import org.vitrivr.cineast.core.features.EdgeARP88;
import org.vitrivr.cineast.core.features.EdgeARP88Full;
import org.vitrivr.cineast.core.features.EdgeGrid16;
import org.vitrivr.cineast.core.features.EdgeGrid16Full;
import org.vitrivr.cineast.core.features.HueValueVarianceGrid8;
import org.vitrivr.cineast.core.features.MedianColor;
import org.vitrivr.cineast.core.features.MedianColorARP44;
import org.vitrivr.cineast.core.features.MedianColorGrid8;
import org.vitrivr.cineast.core.features.MedianColorRaster;
import org.vitrivr.cineast.core.features.MedianFuzzyHist;
import org.vitrivr.cineast.core.features.MotionHistogram;
import org.vitrivr.cineast.core.features.MotionSum;
import org.vitrivr.cineast.core.features.SaturationGrid8;
import org.vitrivr.cineast.core.features.SubDivAverageFuzzyColor;
import org.vitrivr.cineast.core.features.SubDivMedianFuzzyColor;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram2;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram3;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram4;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram5;
import org.vitrivr.cineast.core.features.SubDivMotionSum2;
import org.vitrivr.cineast.core.features.SubDivMotionSum3;
import org.vitrivr.cineast.core.features.SubDivMotionSum4;
import org.vitrivr.cineast.core.features.SubDivMotionSum5;
import org.vitrivr.cineast.core.features.SubtitleFulltextSearch;
import org.vitrivr.cineast.core.features.SubtitleWordSearch;
import org.vitrivr.cineast.core.features.exporter.QueryImageExporter;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.core.run.ExtractionJobRunner;
import org.vitrivr.cineast.core.run.FeatureExtractionRunner;
import org.vitrivr.cineast.core.util.LogHelper;

import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * Entry point. 
 * Has an executable main class which connects to the DB and opens a connection to the webserver
 * Ports and additional settings can be specified at cineast.properties
 */
public class API {

	private static RetrieverInitializer initializer = new RetrieverInitializer() {

		@Override
		public void initialize(Retriever r) {
			r.init(new ADAMproSelector());

		}
	};

	private static final Pattern inputSplitPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
	
	private static Logger LOGGER = LogManager.getLogger();
	
	private static boolean running = true;

	public static void main(String[] args) {
		// TODO parse command line arguments

		CommandLine commandline = handleCommandLine(args);
		
		if(commandline.hasOption("config")){
			Config.parse(new File(commandline.getOptionValue("config")));
		}
		
		boolean disableAllAPI = false;
		
		if(commandline.hasOption("job")){
			ExtractionJobRunner ejr = new ExtractionJobRunner(new File(commandline.getOptionValue("job")));
			Thread thread = new Thread(ejr);
			thread.start();
			disableAllAPI = true;
		}
		
		if(!disableAllAPI && Config.getApiConfig().getEnableCli() || commandline.hasOption('i')){
			APICLIThread cli = new APICLIThread();
			cli.start();
		}
		
		if(!disableAllAPI && Config.getApiConfig().getEnableJsonAPI()){
			try {
				ServerSocket ssocket = new ServerSocket(Config.getApiConfig().getJsonApiPort());
				/*
				 * Wait for a connection, Open a new Thread for each connection.
				 */
				while (running) {
					JSONAPIThread thread = new JSONAPIThread(ssocket.accept());
					thread.start();
				}
				ssocket.close();
			} catch (IOException e) {
				LOGGER.fatal(LogHelper.getStackTrace(e));
			}
			LOGGER.info("Exiting...");
		}
	}

	private static CommandLine handleCommandLine(String[] args) {
		Options options = new Options();
		
		options.addOption("h", "help", false, "print this message");
		options.addOption("i", "interactive", false, "enables the CLI independently of what is specified in the config");
		
		Option configLocation = new Option(null, "config", true, "alternative config file, by default 'cineast.json' is used");
		configLocation.setArgName("CONFIG_FILE");
		options.addOption(configLocation);
		
		Option extractionJob = new Option(null, "job", true, "job file containing settings for extraction");
		configLocation.setArgName("JOB_FILE");
		options.addOption(extractionJob);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine line;
	    try {
	         line = parser.parse( options, args );
	    }catch(ParseException e) {
	    	LOGGER.error("Error parsing command line arguments: {}", e.getMessage());
	    	return null;
	    }

	    if(line.hasOption("help")){
	    	HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp("cineast", options);
	    }
	    
		return line;
	}

	public static RetrieverInitializer getInitializer() {
		return initializer;
	}
	
	public static TObjectDoubleHashMap<Retriever> getRetrieversByCategory(String category){
		
		String c = category.toLowerCase();
		TObjectDoubleHashMap<Retriever> _return = new TObjectDoubleHashMap<Retriever>();
		
		switch(c){
		case "globalcolor":{
			_return.put(new AverageColor(),				2.3);
			_return.put(new DominantColors(),			1.0);
			_return.put(new MedianColor(),				1.2);
			_return.put(new QueryImageExporter(), 		0.001);
			return _return;
		}
		case "localcolor":{
			_return.put(new AverageFuzzyHist(),			0.7);
			_return.put(new MedianFuzzyHist(),			1.3);
			_return.put(new AverageColorARP44(),		0.5);
			_return.put(new MedianColorARP44(),			0.85);
			_return.put(new SubDivAverageFuzzyColor(),	0.5);
			_return.put(new SubDivMedianFuzzyColor(),	0.85);
			_return.put(new AverageColorGrid8(),		1.8);
			_return.put(new ChromaGrid8(),				0.95);
			_return.put(new SaturationGrid8(),			0.65);
			_return.put(new AverageColorCLD(),			1.4);
			_return.put(new CLD(),						1.3);
			_return.put(new HueValueVarianceGrid8(),	0.85);
			_return.put(new MedianColorGrid8(),			1.7);
			_return.put(new AverageColorRaster(),		1.0);
			_return.put(new MedianColorRaster(),		1.0);
			return _return;
		}
		case "edge":{
			_return.put(new EdgeARP88(),				0.85);
			_return.put(new EdgeGrid16(),				1.15);
			_return.put(new EdgeARP88Full(),			0.85);
			_return.put(new EdgeGrid16Full(),			0.85);
			_return.put(new EHD(),						0.7);
			_return.put(new DominantEdgeGrid16(),		1.4);
			_return.put(new DominantEdgeGrid8(),		1.4);
			return _return;
		}
		case "motion":{
			_return.put(new MotionHistogram(),			1.0);
			_return.put(new SubDivMotionHistogram2(),	1.0);
			_return.put(new SubDivMotionHistogram3(),	1.0);
			_return.put(new SubDivMotionHistogram4(),	1.0);
			_return.put(new SubDivMotionHistogram5(),	1.0);
			_return.put(new MotionSum(),				1.0);
			_return.put(new SubDivMotionSum2(),			1.0);
			_return.put(new SubDivMotionSum3(),			1.0);
			_return.put(new SubDivMotionSum4(),			1.0);
			_return.put(new SubDivMotionSum5(),			1.0);
			return _return;
		}
		
		case "meta":{
			_return.put(new SubtitleWordSearch(), 1.0);
			_return.put(new SubtitleFulltextSearch(), 1.0);
			return _return;
		}
		case "all":{
			_return.put(new AverageColor(),				2.3 * 2);
			_return.put(new DominantColors(),			1.0 * 2);
			_return.put(new MedianColor(),				1.2 * 2);
			
			_return.put(new AverageFuzzyHist(),			0.7 * 6);
			_return.put(new MedianFuzzyHist(),			1.3 * 6);
			_return.put(new AverageColorARP44(),		0.5 * 6);
			_return.put(new MedianColorARP44(),			0.85 * 6);
			_return.put(new SubDivAverageFuzzyColor(),	0.5 * 6);
			_return.put(new SubDivMedianFuzzyColor(),	0.85 * 6);
			_return.put(new AverageColorGrid8(),		1.8 * 6);
			_return.put(new ChromaGrid8(),				0.95 * 6);
			_return.put(new SaturationGrid8(),			0.65 * 6);
			_return.put(new AverageColorCLD(),			1.4 * 6);
			_return.put(new CLD(),						1.3 * 6);
			_return.put(new HueValueVarianceGrid8(),	0.85 * 6);
			_return.put(new MedianColorGrid8(),			1.7 * 6);
			
			_return.put(new EdgeARP88(),				0.85 * 3);
			_return.put(new EdgeGrid16(),				1.15 * 3);
			_return.put(new EdgeARP88Full(),			0.85 * 3);
			_return.put(new EdgeGrid16Full(),			0.85 * 3);
			_return.put(new EHD(),						0.7 * 3);
			_return.put(new DominantEdgeGrid16(),		1.4 * 3);
			_return.put(new DominantEdgeGrid8(),		1.4 * 3);
			
			_return.put(new MotionHistogram(),			1.0);
			_return.put(new SubDivMotionHistogram2(),	1.0);
			_return.put(new SubDivMotionHistogram3(),	1.0);
			_return.put(new SubDivMotionHistogram4(),	1.0);
			_return.put(new SubDivMotionHistogram5(),	1.0);
			_return.put(new MotionSum(),				1.0);
			_return.put(new SubDivMotionSum2(),			1.0);
			_return.put(new SubDivMotionSum3(),			1.0);
			_return.put(new SubDivMotionSum4(),			1.0);
			_return.put(new SubDivMotionSum5(),			1.0);
			
//			_return.put(new SubtitleWordSearch(), 1.0);
//			_return.put(new SubtitleFulltextSearch(), 1.0);
//			_return.put(new RelativePosition(), 1.0);
			return _return;
		}
		}
		
		return _return;
		
	}

	private static final class APICLIThread extends Thread{
		
		@Override
		public void run(){
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty()) {
						continue;
					}
					Matcher matcher = inputSplitPattern.matcher(line);
					List<String> commands = new ArrayList<String>();
					while (matcher.find()) {
						commands.add(matcher.group(1).replace("\"", ""));
					}
	
					if (commands.isEmpty()) {
						continue;
					}
					switch (commands.get(0).toLowerCase()) {
					case "extract": {
						if (commands.size() < 2) {
							System.out.println("expected base folder of video to extract");
							break;
						}
						File videoFolder = new File(commands.get(1));
						if (!videoFolder.exists() || !videoFolder.isDirectory()) {
							System.out.println("expected base folder of video to extract: "
									+ videoFolder.getAbsolutePath() + " is not a folder");
							break;
						}
						FeatureExtractionRunner runner = new FeatureExtractionRunner();
						runner.extractFolder(videoFolder);
						break;
					}
					case "exit":
					case "quit": {
						running = false;
						System.exit(0);
						break;
					}
					default:
						System.err.println("unrecognized command: " + line);
					}
				}
			} catch (IOException e) {
				//ignore
			}
		}
		
	}
	
}