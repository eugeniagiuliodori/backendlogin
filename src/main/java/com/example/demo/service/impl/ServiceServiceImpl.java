package com.example.demo.service.impl;

import com.example.demo.dao.IServiceDao;
import com.example.demo.entity.EService;
import com.example.demo.entity.EUser;
import com.example.demo.service.interfaces.IServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

public class ServiceServiceImpl implements IServiceService {


    @Autowired
    private IServiceDao serviceDao;


    @Override
    public EService findByName(String name) {
        return serviceDao.findByName(name);
    }

    @Override
    public List<EService> findAll() {
        return serviceDao.findAll();
    }

    @Override
    public Optional<EService> findById(Long id) {
        return serviceDao.findById(id);
    }

    @Override
    public void flush() {
        serviceDao.flush();
    }

    @Override
    public EService save(EService service) {
        return serviceDao.save(service);
    }

    @Override
    public void delete(EService service) {
        serviceDao.delete(service);
    }

    @Override
    public void deleteByName(String name) {
        serviceDao.deleteByName(name);
    }

    @Override
    public void deleteById(Long id) {
        serviceDao.deleteById(id);
    }

    @Override
    public void deleteAll() {
        serviceDao.deleteAll();
    }

    @Override
    public long count() {
        return serviceDao.count();
    }
}
