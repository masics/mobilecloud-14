package org.magnum.dataup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoSrv {

	private Map<Long, Video> videos = new HashMap<Long, Video>();
	private static final AtomicLong currentId = new AtomicLong(0L);

	VideoFileManager fileManager;

	public VideoSrv() throws IOException {
		fileManager = VideoFileManager.get();
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody List<Video> getVideoList() {
		return new ArrayList<Video>(videos.values());
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		save(v);
		v.setDataUrl(getDataUrl(v.getId()));
		return v;
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<VideoStatus> setVideoData(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			@RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData)
			throws IOException {
		Video v = videos.get(id);
		if (v == null) {
			return new ResponseEntity<VideoStatus>(HttpStatus.NOT_FOUND);
		}
		InputStream in = videoData.getInputStream();
		fileManager.saveVideoData(v, in);
		return new ResponseEntity<VideoStatus>(new VideoStatus(
				VideoStatus.VideoState.READY), HttpStatus.OK);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<byte[]> getData(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id
			) throws IOException {
		Video v = videos.get(id);
		if (v == null || !fileManager.hasVideoData(v)) {
			throw new VideoDataNotFoundException();
			//return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		fileManager.copyVideoData(v, os);
		return new ResponseEntity<byte[]>(os.toByteArray(), HttpStatus.OK);
	}

	
/*	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	public void getData(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			HttpServletResponse response
			) throws IOException {
		final Video v = videos.get(id);
		if (v == null || !fileManager.hasVideoData(v)) {
			response.sendError(404);
			return;
		}
		fileManager.copyVideoData(v, response.getOutputStream());
		return;
	}
*/	
/*	
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getData(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			HttpServletResponse response) throws IOException {
		final Video v = videos.get(id);
		if (v == null || !fileManager.hasVideoData(v)) {
			//response.sendError(404);
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}
		fileManager.copyVideoData(v, response.getOutputStream());
		return new ResponseEntity<String>(HttpStatus.OK);
	}
*/
	private String getDataUrl(long videoId) {
		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
		return url;
	}

	private String getUrlBaseForLocalServer() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		String base = "http://"
				+ request.getServerName()
				+ ((request.getServerPort() != 80) ? ":"
						+ request.getServerPort() : "");
		return base;
	}

	public Video save(Video entity) {
		checkAndSetId(entity);
		videos.put(entity.getId(), entity);
		return entity;
	}

	private void checkAndSetId(Video entity) {
		if (entity.getId() == 0) {
			entity.setId(currentId.incrementAndGet());
		}
	}

}
