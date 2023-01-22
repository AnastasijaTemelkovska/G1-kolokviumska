package mk.ukim.finki.wp.kol2022.g1.service.impl;

import mk.ukim.finki.wp.kol2022.g1.model.Employee;
import mk.ukim.finki.wp.kol2022.g1.model.EmployeeType;
import mk.ukim.finki.wp.kol2022.g1.model.Skill;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidEmployeeIdException;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidSkillIdException;
import mk.ukim.finki.wp.kol2022.g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2022.g1.repository.SkillRepository;
import mk.ukim.finki.wp.kol2022.g1.service.EmployeeService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EmployeeServiceImpl implements EmployeeService, UserDetailsService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SkillRepository skillRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, SkillRepository skillRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.skillRepository = skillRepository;
    }

    public List<Employee> listAll() {
        return this.employeeRepository.findAll();
    }

    public Employee findById(Long id) {
        return (Employee)this.employeeRepository.findById(id).orElseThrow(InvalidEmployeeIdException::new);
    }

    public Employee create(String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        List<Skill> skills = this.skillRepository.findAllById(skillId);
        Employee e = new Employee(name, email, this.passwordEncoder.encode(password), type, skills, employmentDate);
        return (Employee)this.employeeRepository.save(e);
    }

    public Employee update(Long id, String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        Employee e = this.findById(id);
        List<Skill> skills = this.skillRepository.findAllById(skillId);
        e.setEmail(email);
        e.setName(name);
        e.setPassword(this.passwordEncoder.encode(password));
        e.setType(type);
        e.setSkills(skills);
        e.setEmploymentDate(employmentDate);
        return (Employee)this.employeeRepository.save(e);
    }

    public Employee delete(Long id) {
        Employee e = this.findById(id);
        this.employeeRepository.delete(e);
        return e;
    }

    public List<Employee> filter(Long skillId, Integer yearsOfService) {
        Skill s;
        if (skillId != null & yearsOfService != null) {
            s = (Skill)this.skillRepository.findById(skillId).orElseThrow(InvalidSkillIdException::new);
            return (List)this.employeeRepository.findAllBySkills(s).stream().filter((employee) -> {
                return LocalDate.now().getYear() - employee.getEmploymentDate().getYear() > yearsOfService;
            }).collect(Collectors.toList());
        } else if (skillId != null) {
            s = (Skill)this.skillRepository.findById(skillId).orElseThrow(InvalidSkillIdException::new);
            return this.employeeRepository.findAllBySkills(s);
        } else {
            return yearsOfService != null ? (List)this.employeeRepository.findAll().stream().filter((employee) -> {
                return LocalDate.now().getYear() - employee.getEmploymentDate().getYear() > yearsOfService;
            }).collect(Collectors.toList()) : this.employeeRepository.findAll();
        }
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee user = (Employee)this.employeeRepository.findByEmail(username).orElseThrow(() -> {
            return new UsernameNotFoundException(username);
        });
        return new User(user.getEmail(), user.getPassword(), (Collection)Stream.of(new SimpleGrantedAuthority("ROLE_" + user.getType().toString())).collect(Collectors.toList()));
    }
}
