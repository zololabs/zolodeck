describe ("User" , function() {
    
    describe ("when user is called login", function(){
        
        beforeEach(function () {
            this.user = new User();
            this.user.login("Facebook");
        });

        it("should be Logged in", function(){
            expect(this.user.isLoggedIn()).toBeTruthy();
        });

    });

    describe ("when user is called logout", function(){
        
        beforeEach(function () {
            this.user = new User();
            this.user.login("Facebook");
            this.user.logout();
        });

        it("should be Logged Out", function(){
            expect(this.user.isLoggedIn()).toBeFalsy();
        });

    });
});

