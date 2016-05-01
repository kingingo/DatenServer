package dev.wolveringer.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;

//CREATE TABLE IF NOT EXISTS `report_reports` ( `reportId` INT NOT NULL PRIMARY KEY , `reporter` INT NOT NULL , `reporterIp` VARCHAR(100) NOT NULL , `target` INT NOT NULL , `reson` TEXT NOT NULL , `info` TEXT NOT NULL , `timestamp` BIGINT NOT NULL , `open` BOOLEAN NOT NULL ) ENGINE = InnoDB;
//CREATE TABLE IF NOT EXISTS `report_workers` ( `reportId` INT NOT NULL , `playerId` INT NOT NULL ,`start` BIGINT NOT NULL ,`end` BIGINT NOT NULL) ENGINE = InnoDB;

public class ReportManager {
	private static ReportManager instance;
	
	public static ReportManager getInstance() {
		return instance;
	}
	public static void setInstance(ReportManager instance) {
		ReportManager.instance = instance;
	}
	
	private static int createReportId() {
		return (int) (System.currentTimeMillis() % (Math.pow(2, 31)));
	}

	private ArrayList<ReportEntity> entities = new ArrayList<>();

	public void load() {
		HashMap<Integer, ArrayList<ReportWorker>> workers = new InitHashMap<Integer, ArrayList<ReportWorker>>() {
			@Override
			public ArrayList<ReportWorker> defaultValue(Integer key) {
				return new ArrayList<>();
			}
		};
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `reportId`, `playerId`, `start`, `end` FROM `report_workers`", -1);
		for (String[] sworker : query) {
			try {
				workers.get(Integer.parseInt(sworker[0])).add(new ReportWorker(Integer.parseInt(sworker[0]), Integer.parseInt(sworker[1]), Long.parseLong(sworker[2]), Long.parseLong(sworker[3])));
			} catch (Exception e) {
				System.err.println("Cant serelize report worker " + StringUtils.join(sworker, ":"));
			}
		}

		query = MySQL.getInstance().querySync("SELECT `reportId`, `reporter`, `reporterIp`, `target`, `reson`, `info`, `timestamp`, `open` FROM `report_reports`", -1);
		for (String[] sreport : query) {
			try {
				int rid = Integer.parseInt(sreport[0]);
				ReportEntity e = new ReportEntity(rid, Integer.parseInt(sreport[1]), sreport[2], Integer.parseInt(sreport[3]), sreport[4], sreport[5], Long.parseLong(sreport[6]), sreport[7].equalsIgnoreCase("1") || sreport[7].equalsIgnoreCase("true"), workers.get(rid));
			} catch (Exception e) {
				System.err.println("Cant serelize report " + StringUtils.join(sreport, ":"));
			}
		}
	}

	public int createReport(int reporter, int target, String reson, String info) {
		OnlinePlayer preporter = PlayerManager.getPlayer(reporter);
		String ip = preporter == null || preporter.getCurruntIp() == null ? "undefined" : preporter.getCurruntIp();
		ReportEntity e = new ReportEntity(createReportId(), reporter, ip, target, reson, info, System.currentTimeMillis(), true, new ArrayList<>());
		entities.add(e);
		MySQL.getInstance().command("INSERT INTO `report_reports`(`reportId`, `reporter`, `reporterIp`, `target`, `reson`, `info`, `timestamp`, `open`) VALUES ('"+e.getReportId()+"','"+e.getReporter()+"','"+e.getReporterIp()+"','"+e.getTarget()+"','"+e.getReson()+"','"+e.getInfos()+"','"+e.getTime()+"','"+(e.isOpen() ? "1":"0")+"')");
		return e.getReportId();
	}

	public List<ReportEntity> getOpenReports() {
		List<ReportEntity> reports = new ArrayList<>();
		for (ReportEntity e : new ArrayList<>(entities))
			if (e.isOpen())
				reports.add(e);
		return reports;
	}

	public List<ReportEntity> getReportsFor(int playerId) {
		return getReportsFor(playerId, false);
	}

	public List<ReportEntity> getReportsFor(int playerId, boolean open) {
		List<ReportEntity> reports = new ArrayList<>();
		for (ReportEntity e : new ArrayList<>(entities))
			if (e.getReporter() == playerId)
				if (e.isOpen() || open)
					reports.add(e);
		return reports;
	}

	public List<ReportEntity> getReportsFrom(int playerId) {
		return getReportsFrom(playerId, true);
	}

	public List<ReportEntity> getReportsFrom(int playerId, boolean open) {
		List<ReportEntity> reports = new ArrayList<>();
		for (ReportEntity e : new ArrayList<>(entities))
			if (e.getTarget() == playerId)
				if (e.isOpen() || open)
					reports.add(e);
		return reports;
	}

	public List<ReportWorker> getWorkers(int reportId) {
		for (ReportEntity e : new ArrayList<>(entities))
			if (e.getReportId() == reportId)
				return Collections.unmodifiableList(e.getWorkers());
		return new ArrayList<>();
	}

	public ReportEntity getReportEntity(int reportId) {
		for (ReportEntity e : new ArrayList<>(entities))
			if (e.getReportId() == reportId)
				return e;
		return null;
	}
	
	public void addWorker(ReportEntity e,int playerId){
		ReportWorker w = new ReportWorker(e.getReportId(), playerId, System.currentTimeMillis(), -1);
		e.getWorkers().add(w);
		MySQL.getInstance().command("INSERT INTO `report_workers`(`reportId`, `playerId`, `start`, `end`) VALUES ('"+e.getReportId()+"','"+playerId+"','"+System.currentTimeMillis()+"','-1')");
	}
	public void doneWorker(ReportEntity e,int playerId){
		ReportWorker w = null;
		for(ReportWorker dw : e.getWorkers())
			if(dw.getPlayerId() == playerId){
				w.setEnd(System.currentTimeMillis());
				MySQL.getInstance().command("UPDATE `report_workers` SET `end`='"+System.currentTimeMillis()+"' WHERE `reportId`='"+w.getPlayerId()+"' AND `playerId`='"+playerId+"'");
			}
	}

	public void saveReportEntity(ReportEntity report) {
		String command = "UPDATE `report_reports` SET `reporter`='" + report.getReporter() + "',`reporterIp`='" + report.getReporterIp() + "',`target`='" + report.getTarget() + "',`reson`='" + report.getReson() + "',`info`='" + report.getInfos() + "',`open`='" + (report.isOpen() ? "1" : "0") + "' WHERE `reportId`='" + report.getReportId() + "'";
		MySQL.getInstance().command(command);
	}
	public void closeReport(ReportEntity e) {
		e.setOpen(false);
		saveReportEntity(e);
	}
}
