package cn.glh.alumni.controller.admin;

import cn.glh.alumni.entity.Activity;
import cn.glh.alumni.entity.Album;
import cn.glh.alumni.service.AlbumService;
import cn.glh.alumni.util.AlumniUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Controller("album_admin")
@RequestMapping("/admin/album")
public class AlbumController {
    @Resource
    private AlbumService albumService;

    @GetMapping("/list")
    public String getListPage(){
        return "/admin/album/list";
    }

    @GetMapping("/add")
    public String getAddPage(){
        return "/admin/album/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public String AddAlbum(@RequestBody Album album){
        int i = albumService.insertAlbum(album);
        return i > 0 ? AlumniUtil.getJSONString(0, "新增成功") : AlumniUtil.getJSONString(1, "新增失败");
    }

    @GetMapping("/all")
    @ResponseBody
    public String AllAlbum(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit){
        List<Album> albumList = albumService.queryByPage(page, limit);
        return AlumniUtil.getAdminJSONString(0,"成功",albumList.size(), albumList);
    }

    @GetMapping("/edit/{id}")
    public String editAlbum(Model model, @PathVariable("id") Integer id){
        Album album = albumService.selectById(id);
        model.addAttribute("album", album);
        return "/admin/album/edit";
    }

    @PostMapping("/edit")
    @ResponseBody
    public String editAlbum(@RequestBody Album album){
        int i = albumService.updateAlbum(album);
        return i > 0 ? AlumniUtil.getJSONString(0, "修改成功") : AlumniUtil.getJSONString(1, "修改失败");
    }

    @DeleteMapping("/remove/{id}")
    @ResponseBody
    public String removeAlbum(@PathVariable("id") Integer id){
        int i = albumService.deleteById(id);
        return i > 0 ? AlumniUtil.getJSONString(0, "删除成功") : AlumniUtil.getJSONString(1, "删除失败");
    }

    @GetMapping("/audit")
    public String getAuditPage(){
        return "/admin/album/audit";
    }

    @GetMapping("/audit/list")
    @ResponseBody
    public String auditList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit){
        List<Album> albumList = albumService.queryAuditByPage(page, limit);
        return AlumniUtil.getAdminJSONString(0,"成功",albumList.size(), albumList);
    }

    @GetMapping("/audit/details/{id}")
    public String getDetailPage(Model model, @PathVariable("id") Integer id){
        Album album = albumService.selectById(id);
        model.addAttribute("album", album);
        return "/admin/album/details";
    }

    @PostMapping("/audit/pass/{id}")
    @ResponseBody
    public String passAlbum(@PathVariable("id") Integer id){
        int i = albumService.updateState(id, 1);
        return i > 0 ? AlumniUtil.getJSONString(0, "审核成功") : AlumniUtil.getJSONString(1, "审核失败");
    }

    @PostMapping("/audit/noPass/{id}")
    @ResponseBody
    public String noPassAlbum(@PathVariable("id") Integer id){
        int i = albumService.updateState(id, 2);
        return i > 0 ? AlumniUtil.getJSONString(0, "审核成功") : AlumniUtil.getJSONString(1, "审核失败");
    }

    @PostMapping("/audit/batchPass/{ids}")
    @ResponseBody
    public String batchPassAlbum(@PathVariable("ids") List<Integer> ids){
        try {
            albumService.updateStateList(ids, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return AlumniUtil.getJSONString(1, "审核失败");
        }
        return AlumniUtil.getJSONString(0, "审核成功");
    }

    @PostMapping("/audit/batchNoPass/{ids}")
    @ResponseBody
    public String batchNoPassAlbum(@PathVariable("ids") List<Integer> ids){
        try {
            albumService.updateStateList(ids, 2);
        } catch (Exception e) {
            e.printStackTrace();
            return AlumniUtil.getJSONString(1, "审核失败");
        }
        return AlumniUtil.getJSONString(0, "审核成功");
    }

    @GetMapping("/search")
    @ResponseBody
    public String editAlbum(@RequestParam("title") String title, @RequestParam("sort") Integer sort,
                               @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "limit", defaultValue = "10") Integer limit){
        List<Album> albumList = albumService.searchAlbum(title, sort);
        return AlumniUtil.getAdminJSONString(0,"成功",albumList.size(), albumList);
    }
}
