package dev.wolveringer.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.PlayerManager;

//CREATE TABLE IF NOT EXISTS `report_reports` ( `reportId` INT NOT NULL PRIMARY KEY , `reporter` INT NOT NULL , `reporterIp` VARCHAR(100) NOT NULL , `target` INT NOT NULL , `reson` TEXT NOT NULL , `info` TEXT NOT NULL , `timestamp` BIGINT NOT NULL , `open` BOOLEAN NOT NULL ) ENGINE = InnoDB;
//CREATE TABLE IF NOT EXISTS `report_workers` ( `reportId` INT NOT NULL , `playerId` INT NOT NULL ,`start` BIGINT NOT NULL ,`end` BIGINT NOT NULL) ENGINE = InnoDB;

public class ReportManager {
	private static int createReportId(){
		return (int) (System.currentTimeMillis()%(Math.pow(2, 31)));
	}
	
	private ArrayList<ReportEntity> entities = new ArrayList<>();
	
	
	
	public int createReport(int reporter,int target,String reson,String info){
		OnlinePlayer preporter = PlayerManager.getPlayer(reporter);
		String ip = preporter == null || preporter.getCurruntIp() == null ? "undefined" : preporter.getCurruntIp();
		ReportEntity e = new ReportEntity(createReportId(), reporter, ip, target, reson, info, System.currentTimeMillis(), true, new ArrayList<>());
		entities.add(e);
		return e.getReportId();
	}
	
	public List<ReportEntity> getOpenReports(){
		List<ReportEntity> reports = new ArrayList<>();
		for(ReportEntity e : new ArrayList<>(entities))
			if(e.isOpen())
				reports.add(e);
		return reports;
	}
	
	public List<ReportEntity> getReportsFor(int playerId){
		return getReportsFor(playerId, true);
	}
	
	public List<ReportEntity> getReportsFor(int playerId,boolean open){
		List<ReportEntity> reports = new ArrayList<>();
		for(ReportEntity e : new ArrayList<>(entities))
			if(e.getReporter() == playerId)
				if(e.isOpen() || open)
					reports.add(e);
		return reports;
	}
	
	public List<ReportEntity> getReportsFrom(int playerId){
		return getReportsFrom(playerId, true);
	}
	
	public List<ReportEntity> getReportsFrom(int playerId,boolean open){
		List<ReportEntity> reports = new ArrayList<>();
		for(ReportEntity e : new ArrayList<>(entities))
			if(e.getTarget() == playerId)
				if(e.isOpen() || open)
					reports.add(e);
		return reports;
	}
	
	public List<ReportWorker> getWorkers(int reportId){
		for(ReportEntity e : new ArrayList<>(entities))
			if(e.getReportId() == reportId)
					return Collections.unmodifiableList(e.getWorkers());
		return new ArrayList<>();
	}
	
	public ReportEntity getReportEntity(int reportId){
		for(ReportEntity e : new ArrayList<>(entities))
			if(e.getReportId() == reportId)
				return e;
		return null;
	}
	
	public void saveReportEntity(int reportId){
		
	}
}
