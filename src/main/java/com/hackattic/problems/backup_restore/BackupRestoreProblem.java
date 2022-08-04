package com.hackattic.problems.backup_restore;

import com.hackattic.config.HackatticAdapter;
import com.hackattic.problems.HackAtticProblem;
import com.hackattic.problems.ProblemCleanup;
import com.hackattic.problems.ProblemResult;
import com.hackattic.utils.UrlMaker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupRestoreProblem implements HackAtticProblem, ProblemCleanup {

	public static final String DB_GZIP = "db.gzip";
	private final UrlMaker urlMaker;
	private final HackatticAdapter hackatticAdapter;
	@Autowired
	EntityManager entityManager;

	@Override
	public String problemName() {
		return "backup_restore";
	}

	@SneakyThrows
	@Override
	public void solveProblem() {
		PgDump problemData = hackatticAdapter.getProblemData(urlMaker.getProblemURL(this), PgDump.class);
		byte[] decodedBytes = Base64.getDecoder().decode(problemData.getDump().getBytes());
		writeToDisk(decodedBytes);

		restoreFileToDb();

		var aliveSSNsFromDB = new AliveResult(getAliveSSNsFromDB());

		System.out.println("aliveSSNsFromDB = " + aliveSSNsFromDB);

		ProblemResult problemResult = hackatticAdapter.submitSolution(urlMaker.getSolutionURL(this), aliveSSNsFromDB);

		log.info("Solution result: {}", problemResult);
	}

	private List getAliveSSNsFromDB() {
		final List aliveSSNs = entityManager.createNativeQuery("SELECT distinct ssn from criminal_records where status = 'alive'").getResultList();

		return aliveSSNs;
	}

	@SneakyThrows
	private void restoreFileToDb() {
		Process process = Runtime.getRuntime().exec("createdb hackattic");
		// Process process2 = Runtime.getRuntime().exec("gunzip -c db.gzip | psql hackattic");
		List<String> result;
		List<Process> processes = ProcessBuilder.startPipeline(List.of(
				new ProcessBuilder("gunzip","-c","db.gzip")
						.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE),
				new ProcessBuilder("psql", "hackattic")
						.redirectError(ProcessBuilder.Redirect.INHERIT)
		));
		try(Scanner s = new Scanner(processes.get(processes.size() - 1).getInputStream())) {
			result = s.useDelimiter("\\R").tokens().collect(Collectors.toList());
		}
		result.forEach(System.out::println);
	}

	@SneakyThrows
	private void writeToDisk(byte[] decodedDbDump) {
		byte[] buffer = new byte[1024];

		try (InputStream stream = new ByteArrayInputStream(decodedDbDump);
		     FileOutputStream out = new FileOutputStream(DB_GZIP)) {

			int totalSize;
			while ((totalSize = stream.read(buffer)) > 0) {
				out.write(buffer, 0, totalSize);
			}
		}
	}

	@Override
	public void cleanup() {

	}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class AliveResult {
	List alive_ssns;
}

@Data
class PgDump {
	private String dump;
}