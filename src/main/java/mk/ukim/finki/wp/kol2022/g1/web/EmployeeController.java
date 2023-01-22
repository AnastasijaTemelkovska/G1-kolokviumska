package mk.ukim.finki.wp.kol2022.g1.web;

import mk.ukim.finki.wp.kol2022.g1.model.Employee;
import mk.ukim.finki.wp.kol2022.g1.model.EmployeeType;
import mk.ukim.finki.wp.kol2022.g1.model.Skill;
import mk.ukim.finki.wp.kol2022.g1.service.EmployeeService;
import mk.ukim.finki.wp.kol2022.g1.service.SkillService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class EmployeeController {
    private final EmployeeService service;
    private final SkillService skillService;

    public EmployeeController(EmployeeService service, SkillService skillService) {
        this.service = service;
        this.skillService = skillService;
    }

    @GetMapping({"/", "/employees"})
    public String showList(@RequestParam(required = false) Long skillId, @RequestParam(required = false) Integer yearsOfService, Model model) {
        List<Skill> skills = this.skillService.listAll();
        List employees;
        if (skillId == null && yearsOfService == null) {
            employees = this.service.listAll();
        } else {
            employees = this.service.filter(skillId, yearsOfService);
        }

        model.addAttribute("employees", employees);
        model.addAttribute("skills", skills);
        return "list.html";
    }

    @GetMapping({"/employees/add"})
    public String showAdd(Model model) {
        List<Skill> skills = this.skillService.listAll();
        List<EmployeeType> types = Arrays.asList(EmployeeType.values());
        model.addAttribute("skills", skills);
        model.addAttribute("types", types);
        return "form.html";
    }

    @GetMapping({"/employees/{id}/edit"})
    public String showEdit(@PathVariable Long id, Model model) {
        Employee employee = this.service.findById(id);
        List<Skill> skills = this.skillService.listAll();
        List<EmployeeType> types = Arrays.asList(EmployeeType.values());
        model.addAttribute("employee", employee);
        model.addAttribute("skills", skills);
        model.addAttribute("types", types);
        return "form.html";
    }

    @PostMapping({"/employees"})
    public String create(@RequestParam String name, @RequestParam String email, @RequestParam String password, @RequestParam EmployeeType type, @RequestParam List<Long> skillId, @RequestParam String employmentDate) {
        this.service.create(name, email, password, type, skillId, LocalDate.parse(employmentDate));
        return "redirect:/employees";
    }

    @PostMapping({"/employees/{id}"})
    public String update(@PathVariable Long id, @RequestParam String name, @RequestParam String email, @RequestParam String password, @RequestParam EmployeeType type, @RequestParam List<Long> skillId, @RequestParam String employmentDate) {
        this.service.update(id, name, email, password, type, skillId, LocalDate.parse(employmentDate));
        return "redirect:/employees";
    }

    @PostMapping({"/employees/{id}/delete"})
    public String delete(@PathVariable Long id) {
        this.service.delete(id);
        return "redirect:/employees";
    }
}
