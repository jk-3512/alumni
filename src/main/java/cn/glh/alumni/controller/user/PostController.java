package cn.glh.alumni.controller.user;

import cn.glh.alumni.entity.*;
import cn.glh.alumni.entity.enums.AlumniEnum;
import cn.glh.alumni.service.*;
import cn.glh.alumni.util.HostHolder;
import com.github.pagehelper.PageInfo;
import cn.glh.alumni.base.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 帖子表(Post)表控制层
 *
 * @author makejava
 * @since 2022-02-28 09:28:22
 */
@Controller("post_user")
@RequestMapping("/user/post")
public class PostController {

    @Resource
    private HostHolder hostHolder;

    @Resource
    private LoginService loginService;

    @Resource
    private LikeService likeService;

    @Resource
    private CollectService collectService;

    @Resource
    private CommentService commentService;
    /**
     * 服务对象
     */
    @Resource
    private PostService postService;

    /**
     * 进入到帖子列表页
     * @param model
     * @return
     */
    @GetMapping("/sort/{sort}")
    public String getPostList(
            Model model, @PathVariable(value = "sort") Integer sort,
            @RequestParam(value = "page",required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "limit",required = false, defaultValue = "2") Integer limit,
            @RequestParam(value = "isPage",required = false, defaultValue = "false") Boolean isPage){
        //获取该分类下的所有帖子
        List<Post> postList = postService.getPostList(sort, page, limit);
        List<Map<String, Object>> postVoList = this.getPostList(postList);
        //所有帖子的全部内容
        model.addAttribute("posts", postVoList);
        //分页需要用到
        model.addAttribute("sort",sort);
        //获取该类别下的总条码
        model.addAttribute("count", postService.getCountBySort(sort));
        //除第一次访问外，其余返回th:fragment内容，只更新内容列表
        if (isPage){
            return "/user/post/list::content";
        }
        return "/user/post/list";
    }

    /**
     * 自己发布的帖子
     * @param model
     * @return
     */
    @GetMapping("/my")
    public String getMyPost(Model model){
        User user = hostHolder.getUser();
        if (user == null){
            model.addAttribute("msg", "请先登录!");
            model.addAttribute("target", "/user/login");
            return "/user/system/operate-result";
        }
        List<Post> postList = postService.findByUserId(user.getId());
        List<Map<String, Object>> postVoList = this.getPostList(postList);
        //所有帖子的全部内容
        model.addAttribute("posts", postVoList);
        return "/user/post/list";
    }

    /**
     * 其他人发布的帖子
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/other/{id}")
    public String getOtherPost(Model model, @PathVariable(value = "id") Integer id){
        List<Post> postList = postService.findByUserId(id);
        List<Map<String, Object>> postVoList = this.getPostList(postList);
        //所有帖子的全部内容
        model.addAttribute("posts", postVoList);
        return "/user/post/list";
    }

    /**
     * 进入到帖子发布页面
     * @return String
     */
    @GetMapping("/publish")
    public String getPublishPage(Model model){
        User user = hostHolder.getUser();
        if (user == null){
            model.addAttribute("msg", "请先登录!");
            model.addAttribute("target", "/user/login");
            return "/user/system/operate-result";
        }
        return "/user/post/publish";
    }

    /**
     * 发布帖子
     * @param post
     * @return
     */
    @PostMapping("/add")
    public String addPost(Model model, Post post){
        User user = hostHolder.getUser();
        if (user == null) {
            model.addAttribute("msg", "请先登录!");
            model.addAttribute("target", "/user/login");
        }else {
            postService.insertPost(post);
            model.addAttribute("msg", "帖子发布成功!");
            model.addAttribute("target", "/user/post/category/0");
        }
        return "/user/system/operate-result";
    }

    /**
     * 进入到帖子更新页面
     * @return String
     */
    @GetMapping("/update/{id}")
    public String getUpdatePage(Model model, @PathVariable("id") Integer id){
        User user = hostHolder.getUser();
        if (user == null){
            model.addAttribute("msg", "请先登录!");
            model.addAttribute("target", "/user/login");
            return "/user/system/operate-result";
        }
        Post post = postService.selectById(id);
        model.addAttribute("post", post);
        return "/user/post/update";
    }

    /**
     * 活动更新
     * @return String
     */
    @PostMapping("/update")
    public String updatePost(Model model, Post post){
        User user = hostHolder.getUser();
        if (user == null){
            model.addAttribute("msg", "请先登录!");
            model.addAttribute("target", "/user/login");
        }else {
            postService.updatePost(post);
            model.addAttribute("msg", "帖子更新成功!");
            model.addAttribute("target", "/user/post/my");
        }
        return "/user/system/operate-result";
    }

    /**
     * 获取固定条件下的帖子集合
     * @param postList 固定条件下的帖子
     * @return 帖子集合
     */
    private List<Map<String, Object>> getPostList(List<Post> postList){
        //用来存储所有帖子所属的所有点赞、收藏、评论、回复
        List<Map<String, Object>> postVoList = new ArrayList<>();
        //遍历集合，获取单独的帖子
        if (postList != null){
            for (Post post : postList) {
                //为每一个帖子设置作者
                post.setAuthor(loginService.selectById(post.getUserId()).getNickName());
                //为每一个帖子计算留言数
                int msgNum = 0;
                //存储单个帖子的点赞、收藏、评论、回复
                Map<String, Object> postVo = new HashMap<>();
                //先加入帖子本体
                postVo.put("post", post);
                // 帖子的点赞数量
                long likeCount = likeService.findTargetLikeCount(AlumniEnum.post.getAlumniType(), post.getId());
                // 当前登录用户的点赞状态
                int likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findTargetLikeStatus(AlumniEnum.post.getAlumniType(), post.getId(), hostHolder.getUser().getId());
                postVo.put("likeCount", likeCount);
                postVo.put("likeStatus", likeStatus);

                //帖子收藏数量
                long collectCount = collectService.findTargetCollectCount(AlumniEnum.post.getAlumniType(), post.getId());
                //当前用户的收藏状态
                int collectStatus = hostHolder.getUser() == null ? 0 :
                        collectService.findTargetCollectStatus(AlumniEnum.post.getAlumniType(), post.getId(), hostHolder.getUser().getId());
                postVo.put("collectCount", collectCount);
                postVo.put("collectStatus", collectStatus);

                //遍历评论集合获取每个评论的用户名、头像、内容、时间
                List<Comment> commentList = commentService.findCommentByType(AlumniEnum.post.getCode(), post.getId());
                List<Map<String, Object>> commentVoList = new ArrayList<>();

                if (commentList != null){
                    msgNum += commentList.size();
                    for (Comment comment : commentList) {
                        //存储对活动的评论
                        Map<String, Object> commentVo = new HashMap<>();
                        //评论
                        commentVo.put("comment", comment);
                        //评论人
                        commentVo.put("user", loginService.selectById(comment.getFromUserId()));
                        // 该评论点赞数量
                        likeCount = likeService.findTargetLikeCount(AlumniEnum.comment.getAlumniType(), comment.getId());
                        commentVo.put("likeCount", likeCount);
                        // 当前登录用户对该评论的点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findTargetLikeStatus(
                                AlumniEnum.comment.getAlumniType(), comment.getId(), hostHolder.getUser().getId());
                        commentVo.put("likeStatus", likeStatus);

                        //每个对评论的回复
                        List<Comment> replyList = commentService.findCommentByType(AlumniEnum.comment.getCode(), comment.getId());
                        // 封装对评论的评论和评论的作者信息
                        List<Map<String, Object>> replyVoList = new ArrayList<>();

                        if (replyList != null){
                            //每个评论的回复数
                            msgNum += replyList.size();
                            for (Comment reply : replyList) {
                                Map<String, Object> replyVo = new HashMap<>();
                                //回复
                                replyVo.put("reply", reply);
                                //回复人
                                replyVo.put("user", loginService.selectById(reply.getFromUserId()));
                                // 该回复的点赞数量
                                likeCount = likeService.findTargetLikeCount(AlumniEnum.comment.getAlumniType(), reply.getId());
                                replyVo.put("likeCount", likeCount);
                                // 当前登录用户的点赞状态
                                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findTargetLikeStatus(
                                        AlumniEnum.comment.getAlumniType(), reply.getId(), hostHolder.getUser().getId());
                                replyVo.put("likeStatus", likeStatus);
                                replyVoList.add(replyVo);
                            }
                        }
                        //向评论中填充回复
                        commentVo.put("replys", replyVoList);
                        commentVoList.add(commentVo);
                    }
                }

                //为每一个帖子存储相应的评论、回复
                postVo.put("comments", commentVoList);
                postVo.put("msgNum", msgNum);
                postVoList.add(postVo);
            }
        }
        return postVoList;
    }

    /**
     * 帖子删除
     * @param model
     * @param id 帖子ID
     * @return
     */
    @GetMapping("/delete/{id}")
    public String deletePost(Model model, @PathVariable("id") Integer id){
        User user = hostHolder.getUser();
        if (user == null){
            model.addAttribute("msg", "请先登录!");
            model.addAttribute("target", "/user/login");
        }else {
            postService.deleteById(id);
            model.addAttribute("msg", "帖子删除成功!");
            model.addAttribute("target", "/user/my/oneself");
        }
        return "/user/system/operate-result";
    }

    @GetMapping("/search")
    public String searchPost(Model model, @RequestParam("search") String search){
        List<Post> postList = postService.searchPost(search, null);
        List<Map<String, Object>> postVoList = this.getPostList(postList);
        //所有帖子的全部内容
        model.addAttribute("posts", postVoList);
        model.addAttribute("search", search);
        model.addAttribute("count", postList.size());
        return "/user/post/search";
    }

}

