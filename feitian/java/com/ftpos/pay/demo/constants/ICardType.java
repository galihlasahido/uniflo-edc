package com.ftpos.pay.demo.constants;

/**
 * @author GuoJirui.
 * @date 2021/4/27.
 * @desc Define supported card types
 */

public interface ICardType {
    int TYPE_CARD_CONTACT = 0x01;
    int TYPE_CARD_CONTACT_LESS = 0x02;
    int TYPE_CARD_MAGNETIC = 0x08;
}
