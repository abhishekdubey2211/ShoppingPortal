package com.jodo.portal.constants;

public interface MessageConstants {
	String USER_REDIS_KEY = "USER#";
	String PRODUCT_REDISKEY = "PRODUCT#";
	String GET_ALL_USERS = "ALL_USERS";

	String PRODUCT_INSTOCK = "IN-STOCK";
	String PRODUCT_OUTOFFSTOCK = "OUT-OFF-STOCK";
	int ACTIVE = 1;
	int INACTIVE = 0;

	String ORDER_ITEM_CANCELLED = "ITEM_CANCELLED";
	String ORDER_CANCELLED = "ORDER_CANCELLED";
	String ORDER_PLACED = "ORDER_PLACED";
	String NOT_DELIVERED = "NOT_DELIVERED";
	String ORDER_DELIVERED = "ORDER_DELIVERED";

	String WELCOME_MAIL_SUBJECT = "Welcome to Quick Carts";
	
	String WELCOME_MAIL_BODY = "<h1 style=\"color: #333333;\">Welcome to Quick Carts!</h1>\r\n"
			+ "        <p style=\"font-size: 16px; color: #555555; line-height: 1.5;\">\r\n"
			+ "            Hi $$USERNAME$$,\r\n" + "        </p>\r\n"
			+ "        <p style=\"font-size: 16px; color: #555555; line-height: 1.5;\">\r\n"
			+ "            We are excited to have you on board! Thank you for registering at <strong>Quick Carts</strong>, your new destination for the best online shopping experience.\r\n"
			+ "        </p>\r\n" + "        <p style=\"font-size: 16px; color: #555555; line-height: 1.5;\">\r\n"
			+ "            You can now explore our wide range of products, manage your account, and enjoy great deals and offers just for you.\r\n"
			+ "        </p>\r\n" + "        <p style=\"font-size: 16px; color: #555555; line-height: 1.5;\">\r\n"
			+ "            If you have any questions, feel free to reach out to our support team.\r\n"
			+ "        </p>\r\n" + "        <p style=\"font-size: 16px; color: #555555; line-height: 1.5;\">\r\n"
			+ "            Happy Shopping!\r\n" + "        </p>\r\n"
			+ "        <p style=\"font-size: 16px; color: #555555; line-height: 1.5;\">\r\n"
			+ "            Best regards,<br>\r\n" + "            The Quick Carts Team\r\n" + "        </p>\r\n"
			+ "        <div class=\"footer\" style=\"margin-top: 30px; text-align: center; font-size: 14px; color: #777777;\">\r\n"
			+ "            &copy; 2024 Quick Carts. All rights reserved.\r\n" + "        </div>\r\n"
			+ "    </div>";
	
	String OTP_SUBJECT = "Your Quick Carts OTP Code";
	String OTP_BODY = "Dear $$USERNAME$$,\r\n" + "\r\n" + "Your Quick Carts OTP code is: $$OTP$$\r\n" + "\r\n"
			+ "Please use this code within 2 minutes to complete your verification. This OTP is valid for the next 10 minutes but is recommended to be used within 2 minutes for optimal security. Do not share it with anyone.\r\n"
			+ "\r\n" + "Thank you for choosing Quick Carts!\r\n" + "\r\n" + "Best regards,  \r\n"
			+ "Quick Carts Team\r\n" + "";

}
