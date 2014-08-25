package org.magnum.mobilecloud.video;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.*;

/**
 * Created by michaelst on 24/08/2014.
 */
@Controller
public class VideoController {

//    private final String[] users = {"user11", "user2", "user3", "user4"};
    Random rand = new Random();

    @Resource
    VideoRepository videoRepository;

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH+"/{id}/like", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> likeVideo(
            @PathVariable(value = "id") Long id,
            Principal principal
    ){
        Video v = videoRepository.findOne(id);
        if (v == null) {
//            return new ResponseEntity<String>("No such video exist", HttpStatus.NOT_FOUND);
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }
        String user = getUser(principal);
        if (!v.addLiker(user)) {
//            return new ResponseEntity<String>("User "+user+" has already liked this video" + "; Liked users: " + v.getLikeUsers().toString(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
        }
        videoRepository.save(v);
        return new ResponseEntity<Void>(HttpStatus.OK);

    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH+"/{id}/unlike", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> unlikeVideo(
            @PathVariable(value = "id") Long id,
            Principal principal
    ){
        Video v = videoRepository.findOne(id);
        if (v == null) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }
        String user = getUser(principal);
        if (!v.removeLiker(user)) {
            return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
        }
        videoRepository.save(v);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
    @ResponseBody
    public Video addVideo(
            @RequestBody Video v
    ){
        final Video saved = videoRepository.save(v);
        return saved;
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH+"/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Video> getVideoById(
            @PathVariable Long id
    ){
        final Video v = videoRepository.findOne(id);
        if (v == null) {
            return new ResponseEntity<Video>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Video>(v, HttpStatus.OK);
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH+"/{id}/likedby", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Collection<String>> likedBy(
            @PathVariable Long id
    ){
        final Video v = videoRepository.findOne(id);
        if (v == null) {
            return new ResponseEntity<Collection<String>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Collection<String>>(v.getLikeUsers(), HttpStatus.OK);
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<Video> getVideos(){
        final List<Video> list = new ArrayList<>();
         for(Video v : videoRepository.findAll()){
             list.add(v);
         }
        return list;
    }


    private String getUser(Principal principal){
        //int rnd = rand.nextInt(users.length);
        //return users[rnd];
        return principal.getName();
    }

}
