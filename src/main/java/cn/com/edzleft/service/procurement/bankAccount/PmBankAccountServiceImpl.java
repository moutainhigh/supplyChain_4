package cn.com.edzleft.service.procurement.bankAccount;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.edzleft.dao.procurement.bankAccount.PmBankAccountMapper;
import cn.com.edzleft.entity.Account;
import cn.com.edzleft.entity.BankAccount;
import cn.com.edzleft.entity.SessionInfo;

@Service
@Transactional
public class PmBankAccountServiceImpl implements PmBankAccountService {
    @Autowired
    private PmBankAccountMapper pmpmbankAccountMapper;

    /**
     * 查询当前用户的所有的银行卡
     * @param id
     * @return
     */
    @Override
    public List<BankAccount> queryBankAccountByUserId(Integer id) {
        List<BankAccount> bankAccounts = pmpmbankAccountMapper.selectBankAccountByUserId(id);
        return bankAccounts;
    }

    /**
     * 添加银行账户+设置默认
     * @param bankAccount
     * @return
     */
    @Override
    public int addBankAcount(BankAccount bankAccount,HttpSession session) {
        int i = pmpmbankAccountMapper.insertBankAcount(bankAccount);
        return i;
    }

    /**
     * 根据id删除银行卡
     * @param id
     * @return
     */
    @Override
    public int cutBankAccountById(Integer id) {
        int i = pmpmbankAccountMapper.deleteBankAccountById(id);
        return i;
    }


    /**
     * 设置默认银行卡
     * @param id
     * @param session
     * @param value
     * @return
     */
    @Override
    public int setbankAccount(Integer id,Integer value,HttpSession session) {
        SessionInfo sessions = (SessionInfo) session.getAttribute("sessionInfo");
        Integer userId = sessions.getAdmin().getUserId();
        //查询出为默认地址的所有货运单位
        List<BankAccount> list = pmpmbankAccountMapper.selectBankAccountByUserId(userId);
        //判断是否为默认地址
        if(value == 1){
            //将当前所有默认地址修改为不默认
            for (BankAccount acc : list) {
                if(acc.getDefaultId() == 1){
                    acc.setDefaultId(0);
                    pmpmbankAccountMapper.updatebankAccount(acc);
                }
            }
        }
        //设置当前操作的银行卡为默认卡
        BankAccount bankAcc = new BankAccount();
        bankAcc.setBankAccountId(id);
        bankAcc.setDefaultId(value);
        int i = pmpmbankAccountMapper.updatebankAccount(bankAcc);
        return i;
    }

    /**
     * 添加
     */
	@Override
	public Account queryAcountById(Integer id){
        Account a = pmpmbankAccountMapper.selectAccountById(id);
        return a;
	}

	/**
     *  根据银行卡查询出当前输入银行账号信息
     */
	public  BankAccount queryBankAccountByNumber(String number){
        BankAccount bankAccount = pmpmbankAccountMapper.selectBankAccountByNumber(number);
        return bankAccount;
    }
}

