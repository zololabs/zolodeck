describe ("User" , function() {
    
    describe ("when user is called login", function(){
        
        beforeEach(function () {
            this.user = new User();
            this.user.login("FACEBOOK");
        });

        it("should be Logged in", function(){
            expect(this.user.isLoggedIn()).toBeTruthy();
        });

        it("should be return correct service", function(){
            expect(this.user.service()).toBe("FACEBOOK");
        });

    });

    describe ("when user is called logout", function(){
        
        beforeEach(function () {
            this.user = new User();
            this.user.login("FACEBOOK");
            this.user.logout();
        });

        it("should be Logged Out", function(){
            expect(this.user.isLoggedIn()).toBeFalsy();
        });

    });

});

